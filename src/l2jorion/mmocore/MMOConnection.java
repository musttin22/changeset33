/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2jorion.mmocore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

public class MMOConnection<T extends MMOClient<?>>
{
	private final SelectorThread<T> _selectorThread;
	
	private final Socket _socket;
	
	private final InetAddress _address;
	
	private final ReadableByteChannel _readableByteChannel;
	
	private final WritableByteChannel _writableByteChannel;
	
	private final int _port;
	
	private final NioNetStackList<SendablePacket<T>> _sendQueue;
	
	private final SelectionKey _selectionKey;
	
	private ByteBuffer _readBuffer;
	
	private ByteBuffer _primaryWriteBuffer;
	
	private ByteBuffer _secondaryWriteBuffer;
	
	private volatile boolean _pendingClose;
	
	private T _client;
	
	public MMOConnection(SelectorThread<T> selectorThread, Socket socket, SelectionKey key, boolean tcpNoDelay)
	{
		_selectorThread = selectorThread;
		_socket = socket;
		_address = socket.getInetAddress();
		_readableByteChannel = socket.getChannel();
		_writableByteChannel = socket.getChannel();
		_port = socket.getPort();
		_selectionKey = key;
		
		_sendQueue = new NioNetStackList<>();
		
		try
		{
			_socket.setTcpNoDelay(tcpNoDelay);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}
	
	final void setClient(final T client)
	{
		_client = client;
	}
	
	public final T getClient()
	{
		return _client;
	}
	
	public final void sendPacket(final SendablePacket<T> sp)
	{
		sp._client = _client;
		
		if (_pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			_sendQueue.addLast(sp);
		}
		
		if (!_sendQueue.isEmpty())
		{
			try
			{
				_selectionKey.interestOps(_selectionKey.interestOps() | SelectionKey.OP_WRITE);
			}
			catch (CancelledKeyException e)
			{
			}
		}
	}
	
	final SelectionKey getSelectionKey()
	{
		return _selectionKey;
	}
	
	public final InetAddress getInetAddress()
	{
		return _address;
	}
	
	public final int getPort()
	{
		return _port;
	}
	
	final void close() throws IOException
	{
		_socket.close();
	}
	
	final int read(ByteBuffer buf) throws IOException
	{
		return _readableByteChannel.read(buf);
	}
	
	final int write(ByteBuffer buf) throws IOException
	{
		return _writableByteChannel.write(buf);
	}
	
	final void createWriteBuffer(ByteBuffer buf)
	{
		if (_primaryWriteBuffer == null)
		{
			_primaryWriteBuffer = _selectorThread.getPooledBuffer();
			_primaryWriteBuffer.put(buf);
		}
		else
		{
			final ByteBuffer temp = _selectorThread.getPooledBuffer();
			temp.put(buf);
			
			final int remaining = temp.remaining();
			_primaryWriteBuffer.flip();
			final int limit = _primaryWriteBuffer.limit();
			
			if (remaining >= _primaryWriteBuffer.remaining())
			{
				temp.put(_primaryWriteBuffer);
				_selectorThread.recycleBuffer(_primaryWriteBuffer);
			}
			else
			{
				_primaryWriteBuffer.limit(remaining);
				temp.put(_primaryWriteBuffer);
				_primaryWriteBuffer.limit(limit);
				_primaryWriteBuffer.compact();
				_secondaryWriteBuffer = _primaryWriteBuffer;
			}
			_primaryWriteBuffer = temp;
		}
	}
	
	final boolean hasPendingWriteBuffer()
	{
		return _primaryWriteBuffer != null;
	}
	
	final void movePendingWriteBufferTo(ByteBuffer dest)
	{
		_primaryWriteBuffer.flip();
		dest.put(_primaryWriteBuffer);
		_selectorThread.recycleBuffer(_primaryWriteBuffer);
		_primaryWriteBuffer = _secondaryWriteBuffer;
		_secondaryWriteBuffer = null;
	}
	
	final void setReadBuffer(ByteBuffer buf)
	{
		_readBuffer = buf;
	}
	
	final ByteBuffer getReadBuffer()
	{
		return _readBuffer;
	}
	
	public final boolean isClosed()
	{
		return _pendingClose;
	}
	
	final NioNetStackList<SendablePacket<T>> getSendQueue()
	{
		return _sendQueue;
	}
	
	@SuppressWarnings("unchecked")
	public final void close(SendablePacket<T> sp)
	{
		close(new SendablePacket[]
		{
			sp
		});
	}
	
	public final void close(SendablePacket<T>[] closeList)
	{
		if (_pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			if (!_pendingClose)
			{
				_pendingClose = true;
				_sendQueue.clear();
				for (SendablePacket<T> sp : closeList)
				{
					_sendQueue.addLast(sp);
				}
			}
		}
		
		try
		{
			_selectionKey.interestOps(_selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
		}
		catch (CancelledKeyException e)
		{
		}
		
		_selectorThread.closeConnection(this);
	}
	
	final void releaseBuffers()
	{
		if (_primaryWriteBuffer != null)
		{
			_selectorThread.recycleBuffer(_primaryWriteBuffer);
			_primaryWriteBuffer = null;
			
			if (_secondaryWriteBuffer != null)
			{
				_selectorThread.recycleBuffer(_secondaryWriteBuffer);
				_secondaryWriteBuffer = null;
			}
		}
		
		if (_readBuffer != null)
		{
			_selectorThread.recycleBuffer(_readBuffer);
			_readBuffer = null;
		}
	}
}
