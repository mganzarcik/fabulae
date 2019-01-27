Schemas = (function() {

  var targetObjectAttr = [ "__npcAtDialogue", "__pcAtDialogue", "__initialObject", "__global", "__player"];
  var logTypeAttr = [ "crime", "faction", "combat", "skillcheck", "character", "survival", "inventory", "info", "error", "debug", "condition", "state_machine", "journal", "weather"];
  var skillAttr = ["armor", "sword", "dagger", "axe", "staff", "bow", "unarmed", "thrown", "dualwielding", "dodge", "climbing", "swimming", "hunting", "scouting", "sneaking", "persuasion", "traps", "lockpicking", "somatic", "acoustic", "mind", "focus"];
  var booleanAttr = ["true", "false"];

  var conditions = {
    alreadyVisited: { 
      attrs: {
        location: null
      }
    },
    and: { 
      attrs: {}
    },
    canJoinPlayer: { 
      attrs: {
        targetObject: targetObjectAttr
      }
    },
    canLearnSpell: { 
      attrs: {
        id: null,
        targetObject: targetObjectAttr
      }
    },
    canTradeWithPC: { 
      attrs: {
        targetObject: targetObjectAttr
      }
    },
    characterInParty: { 
      attrs: {
        targetObject: targetObjectAttr,
        character: null
      }
    },
    gameObjectActive: { 
      attrs: {
        id: null
      }
    },
    genderEqualTo: { 
        attrs: {
          gender: null,
          targetObject: targetObjectAttr
        }
      },
    hasItem: { 
      attrs: {
        item: null, 
        targetObject: targetObjectAttr
      }
    },
    hasItemEquipped: { 
      attrs: {
        item: null, 
        targetObject: targetObjectAttr
      }
    },
    hasMeleeWeaponEquipped: { 
      attrs: {
        weaponSkill: skillAttr, 
        dualWielding: booleanAttr,
        targetObject: targetObjectAttr
      }
    },
    hasMeleeWeaponSkill: { 
      attrs: {
        minimumSkillRank: null,
        targetObject: targetObjectAttr
      }
    },
    hasRangedWeaponEquipped: { 
      attrs: {
        weaponSkill: skillAttr, 
        dualWielding: booleanAttr,
        targetObject: targetObjectAttr
      }
    },
    hasRangedWeaponSkill: { 
      attrs: {
        minimumSkillRank: null,
        targetObject: targetObjectAttr
      }
    },
    hasShieldEquipped: { 
      attrs: {
        targetObject: targetObjectAttr
      }
    },
    hasSkill: { 
      attrs: {
        skill: skillAttr,
        rank: null,
        useBase: booleanAttr,
        targetObject: targetObjectAttr
      }
    },
    hasWeaponSkill: { 
      attrs: {
        minimumSkillRank: null,
        targetObject: targetObjectAttr
      }
    },
    isCombatInProgress: { 
      attrs: {}
    },
    knowsSpell: { 
      attrs: {
        id: null,
        targetObject: targetObjectAttr
      }
    },
    metBefore: { 
      attrs: {
        targetObject: targetObjectAttr
      }
    },
    not: { 
      attrs: {}
    },
    or: { 
      attrs: {}
    },
    passedSkillCheck: { 
      attrs: {
        skill: skillAttr,
        targetObject: targetObjectAttr
      }
    },
    questCompleted: { 
      attrs: {
        quest: null
      }
    },
    questInState: { 
      attrs: {
        quest: null,
        state: null
      }
    },
    questStarted: { 
      attrs: {
        quest: null
      }
    },
    questVariableEqualTo: { 
      attrs: {
        quest: null,
        name: null,
        value: null
      }
    },
    questVariableFalse: { 
      attrs: {
        quest: null,
        name: null
      }
    },
    questVariableTrue: { 
      attrs: {
        quest: null,
        name: null
      }
    },
    questVariableUndefined: { 
      attrs: {
        quest: null,
        name: null
      }
    },
    questWasInState: { 
      attrs: {
        quest: null,
        state: null
      }
    },
    raceEqualTo: { 
      attrs: {
        race: null,
        targetObject: targetObjectAttr
      }
    },
    roleEqualTo: { 
      attrs: {
        role: null,
        targetObject: targetObjectAttr
      }
    },
    script: { 
      attrs: {}
    },
    usableInState: { 
      attrs: {
        usable: null,
        state: null
      }
    },
    variableEqualTo: { 
      attrs: {
        name: null,
        value: null,
        targetObject: targetObjectAttr
      }
    },
    variableFalse: { 
      attrs: {
        name: null,
        targetObject: targetObjectAttr
      }
    },
    variableTrue: { 
      attrs: {
        name: null,
        targetObject: targetObjectAttr
      }
    },
    variableUndefined: { 
      attrs: {
        name: null,
        targetObject: targetObjectAttr
      }
    },
    visitedLocation: { 
      attrs: {
        location: null,
        targetObject: targetObjectAttr
      }
    }    
  };

  var actions = {
    addAction: {
      attrs: {
        targetObject: targetObjectAttr
      }
    },
    addToHP: {
      attrs: {
        amount: null,
        targetObject: targetObjectAttr
      }
    },
    addToMP: {
      attrs: {
        amount: null,
        targetObject: targetObjectAttr
      }
    },
    addToSP: {
      attrs: {
        amount: null,
        targetObject: targetObjectAttr
      }
    },
    and: {
      attrs: {}
    },
    disableAI: {
      attrs: {
        character: null,
        targetObject: targetObjectAttr
      }
    },
    displayContainerInventory: {
      attrs: {
        targetObject: targetObjectAttr,
        id: null
      }
    },
    displayDialogue: {
      attrs: {
        targetObject: targetObjectAttr,
        npc: null,
        id: null
      }
    },
    displayStorySequence: {
      attrs: {
        id: null
      }
    },
    dropItem: {
      attrs: {
        targetObject: targetObjectAttr,
        id: null
      }
    },
    enableAI: {
      attrs: {
        character: null,
        targetObject: targetObjectAttr
      }
    },
    fireEvent: {
      attrs: {
        targetObject: targetObjectAttr,
        usable: null,
        event: null
      }
    },
    fireQuestEvent: {
      attrs: {
        event: null,
        quest: null
      }
    },
    giveExperience: {
      attrs: {
        amount: null,
        targetObject: targetObjectAttr
      }
    },
    giveGoldToPlayer: {
      attrs: {
        amount: null
      }
    },
    giveItem: {
      attrs: {
        item: null,
        targetObject: targetObjectAttr
      }
    },
    joinPlayer: {
      attrs: {
        targetObject: targetObjectAttr,
        character: null
      }
    },
    leavePlayer: {
      attrs: {
        targetObject: targetObjectAttr,
        character: null
      }
    },
    logMessage: {
      attrs: {
        message: null,
        logType: logTypeAttr
      }
    },
    modifyDisposition: {
      attrs: {
        faction: null,
        value: null
      }
    },
    rollSkillCheck: {
      attrs: {
        skill: skillAttr,
        targetObject: targetObjectAttr
      }
    },
    script: {
      attrs: {}
    },
    setChatter: {
      attrs: {
        chatter: null,
        targetObject: targetObjectAttr
      }
    },
    setQuestVariable: {
      attrs: {
        name: null,
        value: null,
        quest: null
      }
    },
    setVariable: {
      attrs: {
        name: null,
        value: null,
        targetObject: targetObjectAttr
      }
    },
    startQuest: {
      attrs: {
        quest: null
      }
    },
    startTrading: {
      attrs: {}
    },
    switchToCombatMap: {
      attrs: {
        enemyGroup: null
      }
    },
    switchToMap: {
      attrs: {
        id: null,
        x: null,
        y: null
      }
    }
  };


  return {
    conditions: conditions, 
    actions: actions
  }

})();