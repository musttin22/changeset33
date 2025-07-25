import sys
from l2jorion.game.model.quest import State
from l2jorion.game.model.quest import QuestState
from l2jorion.game.model.quest.jython import QuestJython as JQuest

qn = "293_HiddenVein"

CHRYSOLITE_ORE = 1488
TORN_MAP_FRAGMENT = 1489
HIDDEN_VEIN_MAP = 1490
ADENA = 57
NEWBIE_REWARD = 4
SOULSHOT_FOR_BEGINNERS = 5789

def newbie_rewards(st) :
  # check the player state against this quest newbie rewarding mark.
  player=st.getPlayer()
  newbie = player.isNewbie()
  if newbie | NEWBIE_REWARD != newbie :
     if st.getInt("done")==0 :
        st.set("done","1")
        st.checkNewbieQuests()
        player.setNewbie(newbie|NEWBIE_REWARD)
        st.giveItems(SOULSHOT_FOR_BEGINNERS,6000)
        st.playTutorialVoice("tutorial_voice_026")

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [HIDDEN_VEIN_MAP, CHRYSOLITE_ORE, TORN_MAP_FRAGMENT]

 def onEvent (self,event,st) :
    htmltext = event
    if event == "30535-03.htm" :
      st.set("cond","1")
      st.setState(STARTED)
      st.playSound("ItemSound.quest_accept")
    elif event == "30535-06.htm" :
      st.takeItems(TORN_MAP_FRAGMENT,-1)
      st.exitQuest(1)
      st.playSound("ItemSound.quest_finish")
    elif event == "30539-02.htm" :
      if st.getQuestItemsCount(TORN_MAP_FRAGMENT) >=4 :
        htmltext = "30539-03.htm"
        st.giveItems(HIDDEN_VEIN_MAP,1)
        st.takeItems(TORN_MAP_FRAGMENT,4)
    return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   if npcId != 30535 and id != STARTED : return htmltext
   
   if id == CREATED :
     st.set("cond","0")
   if npcId == 30535 :
     if st.getInt("cond")==0 :
       if player.getRace().ordinal() != 4 :
         htmltext = "30535-00.htm"
         st.exitQuest(1)
       elif player.getLevel() >= 6 :
         htmltext = "30535-02.htm"
         return htmltext
       else:
         htmltext = "30535-01.htm"
         st.exitQuest(1)
     else :
       if st.getQuestItemsCount(CHRYSOLITE_ORE)==0 :
         if st.getQuestItemsCount(HIDDEN_VEIN_MAP)==0 :
           htmltext = "30535-04.htm"
         else :
           newbie_rewards(st)
           htmltext = "30535-08.htm"
           st.giveItems(ADENA,st.getQuestItemsCount(HIDDEN_VEIN_MAP)*1000)
           st.takeItems(HIDDEN_VEIN_MAP,-1)
       else :
         if st.getQuestItemsCount(HIDDEN_VEIN_MAP)==0 :
           newbie_rewards(st)
           htmltext = "30535-05.htm"
           st.giveItems(ADENA,st.getQuestItemsCount(CHRYSOLITE_ORE)*10)
           st.takeItems(CHRYSOLITE_ORE,-1)
         else :
           newbie_rewards(st)
           htmltext = "30535-09.htm"
           st.giveItems(ADENA,st.getQuestItemsCount(CHRYSOLITE_ORE)*10+st.getQuestItemsCount(HIDDEN_VEIN_MAP)*1000)
           st.takeItems(HIDDEN_VEIN_MAP,-1)
           st.takeItems(CHRYSOLITE_ORE,-1)
   elif npcId == 30539 :
      htmltext = "30539-01.htm"
   return htmltext

 def onKill(self,npc,player,isPet):
   st = player.getQuestState(qn)
   if not st : return 
   if st.getState() != STARTED : return 
   
   n = st.getRandom(100)
   if n > 50 :
     st.giveItems(CHRYSOLITE_ORE,1)
     st.playSound("ItemSound.quest_itemget")
   elif n < 5 :
     st.giveItems(TORN_MAP_FRAGMENT,1)
     st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(293,qn,"The Hidden Veins")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(30535)

QUEST.addTalkId(30535)
QUEST.addTalkId(30539)

QUEST.addKillId(20446)
QUEST.addKillId(20447)
QUEST.addKillId(20448)