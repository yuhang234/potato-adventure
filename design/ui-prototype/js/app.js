/**
 * Potato Adventure - Main Application Controller & State Machine
 * Central coordinator for all 10 screens, overlays, radar charts, dialogue, and skill tree
 */

const App = {
  currentScreen: 'title',
  activeModal: null,

  selectedClass: 'berserker',
  heroName: '土豆勇士',

  skillPoints: 3,
  skills: {
    strike: { name: '强击', level: 1, maxLevel: 3, unlocked: true, cost: 1, x: 120, y: 220, parent: null, desc: '基础剑术，造成 120% 物理伤害。' },
    whirlwind: { name: '旋风斩', level: 1, maxLevel: 3, unlocked: true, cost: 1, x: 340, y: 140, parent: 'strike', desc: '旋转利刃席卷周围敌人，造成范围群体伤害。' },
    ironwall: { name: '铁壁战甲', level: 0, maxLevel: 3, unlocked: false, cost: 1, x: 340, y: 300, parent: 'strike', desc: '被动提升 15% 物理护甲，受暴击概率降低。' },
    armorsmash: { name: '破甲重击', level: 0, maxLevel: 2, unlocked: false, cost: 2, x: 580, y: 140, parent: 'whirlwind', desc: '粉碎敌人护甲，降低其防御 30% 持续 3 回合。' },
    berserk: { name: '狂暴之怒', level: 0, maxLevel: 2, unlocked: false, cost: 2, x: 580, y: 300, parent: 'ironwall', desc: '生命低于 30% 时，暴击率暴增 50%！' },
    cataclysm: { name: '泰坦裂地奥义', level: 0, maxLevel: 1, unlocked: false, cost: 3, x: 820, y: 220, parent: 'armorsmash', desc: '终极奥义：跃起撕裂大地，造成毁灭性 350% 暴击伤害！' }
  },

  dialogueState: {
    npc: '村长 托马斯',
    textQueue: '',
    displayedText: '',
    timer: null,
    isTyping: false
  },

  init() {
    console.log("Potato Adventure UI Prototype Initializing...");
    
    // Initialize subsystem engines
    AudioEngine.init();
    InventoryEngine.init();
    BattleEngine.init();
    MapEngine.init();

    this.setupEventListeners();
    this.renderRadarCharts();
    this.renderSkillTree();

    const search = window.location.search || (window.location.hash.startsWith('#') ? '?' + window.location.hash.slice(1) : '');
    const params = new URLSearchParams(search);
    const screen = params.get('screen') || 'title';
    const biome = params.get('biome');
    const modal = params.get('modal');
    const dialogue = params.get('dialogue');

    this.switchScreen(screen);
    if (biome) {
      this.setBiome(biome);
    }
    if (modal) {
      this.openModal(modal);
    }
    if (dialogue) {
      this.openDialogue(dialogue.includes('raine') ? '铁匠 雷恩' : '村长 托马斯');
      this.skipTypewriter();
    }
  },

  setupEventListeners() {
    // Global keyboard navigation (1-0 for fast screen review)
    window.addEventListener('keydown', (e) => {
      // Don't trigger shortcuts if typing in text input
      if (e.target.tagName === 'INPUT') return;

      if (e.key === '1') this.switchScreen('title');
      if (e.key === '2') this.switchScreen('charselect');
      if (e.key === '3') this.switchScreen('overworld');
      if (e.key === '4') this.switchScreen('battle');
      if (e.key === '5') this.openModal('modal-inventory');
      if (e.key === '6') this.openDialogue('村长 托马斯');
      if (e.key === '7') this.openModal('modal-skilltree');
      if (e.key === '8') this.openModal('modal-saveload');
      if (e.key === '9') this.openModal('modal-settings');
      if (e.key === '0') this.switchScreen('victory');
      if (e.key === '-' || e.key === '_') this.switchScreen('gameover');

      if (e.key === 'Escape') {
        this.closeAllModals();
      }
      if (e.key === 'b' || e.key === 'B' || e.key === 'i' || e.key === 'I') {
        if (this.currentScreen === 'overworld') {
          if (this.activeModal === 'modal-inventory') this.closeAllModals();
          else this.openModal('modal-inventory');
        }
      }
      if (e.key === 'k' || e.key === 'K') {
        if (this.currentScreen === 'overworld') {
          if (this.activeModal === 'modal-skilltree') this.closeAllModals();
          else this.openModal('modal-skilltree');
        }
      }
    });

    // Add sound blip to all buttons on hover
    document.querySelectorAll('.pixel-btn, .hotbar-slot, .inv-slot, .char-card, .quick-nav-item').forEach(elem => {
      elem.addEventListener('mouseenter', () => AudioEngine.play('blip'));
    });
  },

  switchScreen(screenId) {
    AudioEngine.play('select');
    this.closeAllModals();
    this.currentScreen = screenId;

    document.querySelectorAll('.screen-view').forEach(s => s.classList.remove('active'));
    const target = document.getElementById(`screen-${screenId}`);
    if (target) {
      target.classList.add('active');
    }

    // Update Quick Nav Active state
    document.querySelectorAll('.quick-nav-item').forEach(item => {
      item.classList.toggle('active', item.dataset.screen === screenId);
    });

    if (screenId === 'overworld') {
      MapEngine.setupBiome(MapEngine.biome);
    } else if (screenId === 'battle') {
      BattleEngine.start('迷雾史莱姆');
    }
  },

  openModal(modalId) {
    AudioEngine.play('select');
    this.closeAllModals();
    this.activeModal = modalId;
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
    }

    if (modalId === 'modal-skilltree') {
      this.renderSkillTree();
    }
  },

  closeAllModals() {
    document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
    this.activeModal = null;
    InventoryEngine.hideTooltip();
    if (this.dialogueState.timer) {
      clearInterval(this.dialogueState.timer);
      this.dialogueState.isTyping = false;
    }
  },

  // ---------------- CHARACTER SELECTION ----------------
  selectClass(className) {
    AudioEngine.play('blip');
    this.selectedClass = className;
    document.querySelectorAll('.char-card').forEach(c => {
      c.classList.toggle('selected', c.dataset.class === className);
    });
    MapEngine.player.heroClass = className;
    BattleEngine.hero.sprite = `hero_${className}`;
    BattleEngine.hero.name = className === 'berserker' ? '土豆狂战士' : (className === 'mage' ? '土豆大魔导' : '土豆游侠');
    
    // Update portrait in inventory
    const dollImg = document.getElementById('doll-hero-img');
    if (dollImg) dollImg.src = `assets/sprites/hero_${className}.png`;
  },

  confirmCharacter() {
    const input = document.getElementById('char-name-input');
    if (input && input.value.trim()) {
      this.heroName = input.value.trim();
    }
    document.getElementById('hud-player-name-text').textContent = `${this.heroName} Lv.3`;
    this.switchScreen('overworld');
  },

  renderRadarCharts() {
    // 6-axis attributes: [HP, MP, ATK, DEF, SPD, CRIT] (max 100)
    const statsData = {
      berserker: [90, 30, 85, 75, 50, 45],
      mage: [50, 95, 90, 40, 60, 70],
      ranger: [65, 55, 75, 55, 90, 85]
    };

    ['berserker', 'mage', 'ranger'].forEach(cls => {
      const canvas = document.getElementById(`radar-${cls}`);
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      const w = canvas.width;
      const h = canvas.height;
      const cx = w / 2;
      const cy = h / 2;
      const radius = 45;
      const numAxes = 6;
      const angleStep = (Math.PI * 2) / numAxes;

      ctx.clearRect(0, 0, w, h);

      // Draw concentric hexagon webs
      [0.33, 0.66, 1.0].forEach(ratio => {
        ctx.strokeStyle = 'rgba(214, 168, 72, 0.25)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        for (let i = 0; i < numAxes; i++) {
          const a = i * angleStep - Math.PI / 2;
          const x = cx + Math.cos(a) * radius * ratio;
          const y = cy + Math.sin(a) * radius * ratio;
          if (i === 0) ctx.moveTo(x, y);
          else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.stroke();
      });

      // Axis spokes & labels
      const labels = ['HP', 'MP', 'ATK', 'DEF', 'SPD', 'CRIT'];
      ctx.fillStyle = '#a0b0c0';
      ctx.font = '10px var(--pixel-font)';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';

      for (let i = 0; i < numAxes; i++) {
        const a = i * angleStep - Math.PI / 2;
        const x = cx + Math.cos(a) * radius;
        const y = cy + Math.sin(a) * radius;
        ctx.strokeStyle = 'rgba(214, 168, 72, 0.3)';
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(x, y);
        ctx.stroke();

        const lx = cx + Math.cos(a) * (radius + 12);
        const ly = cy + Math.sin(a) * (radius + 12);
        ctx.fillText(labels[i], lx, ly);
      }

      // Draw stats polygon
      const vals = statsData[cls];
      ctx.fillStyle = cls === 'berserker' ? 'rgba(231, 76, 60, 0.5)' : (cls === 'mage' ? 'rgba(142, 68, 173, 0.5)' : 'rgba(46, 204, 113, 0.5)');
      ctx.strokeStyle = cls === 'berserker' ? '#e74c3c' : (cls === 'mage' ? '#8e44ad' : '#2ecc71');
      ctx.lineWidth = 2;

      ctx.beginPath();
      for (let i = 0; i < numAxes; i++) {
        const a = i * angleStep - Math.PI / 2;
        const r = radius * (vals[i] / 100);
        const x = cx + Math.cos(a) * r;
        const y = cy + Math.sin(a) * r;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.closePath();
      ctx.fill();
      ctx.stroke();
    });
  },

  // ---------------- NPC DIALOGUE TYPEWRITER ----------------
  openDialogue(npcName = '村长 托马斯') {
    AudioEngine.play('select');
    this.dialogueState.npc = npcName;
    this.openModal('modal-dialogue');

    const portraitImg = document.getElementById('dialogue-portrait-img');
    const nameTag = document.getElementById('dialogue-name-tag');
    
    if (npcName.includes('村长')) {
      portraitImg.src = 'assets/sprites/npc_thomas.png';
      nameTag.textContent = '【村长 托马斯 (Mayor Thomas)】';
      this.startTypewriter('勇敢的土豆旅者啊，欢迎来到丰收原野！最近迷雾森林的史莱姆异动频频，暗黑土豆领主的邪能似乎正在苏醒，请你务必万分小心！');
      this.renderDialogueChoices([
        { text: '1. 我愿前往迷雾森林清缴异动 (接受委托)', action: () => this.dialogueReply('感谢你的正义与勇气！请带上这两瓶生命药水，凯旋后我为你备上黄金烤土豆！') },
        { text: '2. 请问关于暗黑土豆领主的传说是真的吗？', action: () => this.dialogueReply('唉，那是千年前被封印的远古土豆暴君...若封印被毁，整片大陆的土豆都将化为暗影枯叶！') },
        { text: '3. 只是路过村子，祝您身体康健。', action: () => this.closeAllModals() }
      ]);
    } else if (npcName.includes('铁匠')) {
      portraitImg.src = 'assets/sprites/npc_raine.png';
      nameTag.textContent = '【铁匠 雷恩 (Blacksmith Raine)】';
      this.startTypewriter('嘿！瞧瞧谁来了！炉火正旺，这柄新锻造的精钢斩魔剑怎么样？只要带来足够的暗影晶核，我就能为你重铸神器！');
      this.renderDialogueChoices([
        { text: '1. 查看可锻造的神话装备列表', action: () => this.dialogueReply('目前可强化：暗黑斩魔剑、守护坠饰！请先在冒险中收集远古精铁与魔核！') },
        { text: '2. 维修目前已磨损的铠甲', action: () => { AudioEngine.play('coin'); this.dialogueReply('叮叮当当！装备耐久已全部恢复满值！随时准备下一场恶战！'); } },
        { text: '3. 告辞离开。', action: () => this.closeAllModals() }
      ]);
    }
  },

  startTypewriter(fullText) {
    if (this.dialogueState.timer) clearInterval(this.dialogueState.timer);

    this.dialogueState.textQueue = fullText;
    this.dialogueState.displayedText = '';
    this.dialogueState.isTyping = true;

    const textElem = document.getElementById('dialogue-text-body');
    textElem.textContent = '';

    let charIdx = 0;
    this.dialogueState.timer = setInterval(() => {
      if (charIdx < fullText.length) {
        this.dialogueState.displayedText += fullText[charIdx];
        textElem.textContent = this.dialogueState.displayedText;
        charIdx++;
      } else {
        clearInterval(this.dialogueState.timer);
        this.dialogueState.isTyping = false;
      }
    }, 25); // 40 chars/sec
  },

  skipTypewriter() {
    if (this.dialogueState.isTyping) {
      clearInterval(this.dialogueState.timer);
      this.dialogueState.isTyping = false;
      document.getElementById('dialogue-text-body').textContent = this.dialogueState.textQueue;
    }
  },

  dialogueReply(text) {
    AudioEngine.play('blip');
    this.startTypewriter(text);
  },

  renderDialogueChoices(choices) {
    const wrap = document.getElementById('dialogue-choices-row');
    wrap.innerHTML = '';
    choices.forEach(ch => {
      const btn = document.createElement('button');
      btn.className = 'dialogue-choice-btn';
      btn.textContent = ch.text;
      btn.onclick = ch.action;
      wrap.appendChild(btn);
    });
  },

  // ---------------- SKILL TREE ----------------
  renderSkillTree() {
    const canvas = document.getElementById('skilltree-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Draw connecting lines between nodes
    Object.keys(this.skills).forEach(key => {
      const node = this.skills[key];
      if (node.parent && this.skills[node.parent]) {
        const parent = this.skills[node.parent];
        ctx.beginPath();
        ctx.moveTo(parent.x + 65, parent.y + 35);
        ctx.lineTo(node.x + 65, node.y + 35);
        ctx.lineWidth = node.unlocked ? 3 : 2;
        ctx.strokeStyle = node.unlocked ? '#ffd264' : '#3c4b57';
        if (!node.unlocked) ctx.setLineDash([6, 6]);
        else ctx.setLineDash([]);
        ctx.stroke();
      }
    });

    // Render Nodes DOM
    const layer = document.getElementById('skilltree-nodes-layer');
    layer.innerHTML = '';

    document.getElementById('skill-sp-count').textContent = this.skillPoints;

    Object.keys(this.skills).forEach(key => {
      const node = this.skills[key];
      const div = document.createElement('div');
      div.className = `skill-node-card ${node.unlocked ? 'unlocked' : ''}`;
      div.style.left = `${node.x}px`;
      div.style.top = `${node.y}px`;

      div.innerHTML = `
        <div class="skill-node-name">${node.name}</div>
        <div class="skill-node-lvl">等级: ${node.level}/${node.maxLevel}</div>
        <div style="font-size:10px; color:#5dade2; margin-top:2px;">消耗: ${node.cost} SP</div>
      `;

      div.onclick = () => this.upgradeSkill(key);
      div.title = node.desc;
      layer.appendChild(div);
    });
  },

  upgradeSkill(key) {
    const node = this.skills[key];
    if (this.skillPoints < node.cost) {
      AudioEngine.play('hit');
      return;
    }
    if (node.level >= node.maxLevel) return;

    if (node.parent && !this.skills[node.parent].unlocked) {
      AudioEngine.play('hit');
      return; // Pre-requisite not met
    }

    AudioEngine.play('fanfare');
    this.skillPoints -= node.cost;
    node.level++;
    node.unlocked = true;
    this.renderSkillTree();
  },

  resetSkills() {
    AudioEngine.play('coin');
    this.skillPoints = 3;
    Object.keys(this.skills).forEach(key => {
      if (key !== 'strike' && key !== 'whirlwind') {
        this.skills[key].level = 0;
        this.skills[key].unlocked = false;
      }
    });
    this.renderSkillTree();
  },

  // ---------------- SAVE / LOAD SLOTS ----------------
  saveSlot(slotNum) {
    AudioEngine.play('select');
    alert(`【槽位 ${slotNum}】存档保存成功！角色状态与地图进度已封存。`);
  },

  loadSlot(slotNum) {
    AudioEngine.play('fanfare');
    this.closeAllModals();
    this.switchScreen('overworld');
  },

  deleteSlot(slotNum) {
    AudioEngine.play('hit');
    alert(`【槽位 ${slotNum}】存档已清除。`);
  },

  // ---------------- BIOME CHANGE ----------------
  setBiome(biomeName) {
    AudioEngine.play('select');
    MapEngine.setupBiome(biomeName);
    document.querySelectorAll('.hud-biome-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.biome === biomeName);
    });
    const banner = document.getElementById('hud-area-banner');
    if (banner) {
      if (biomeName === 'village') banner.textContent = '【和平原野 · 土豆村落】';
      if (biomeName === 'forest') banner.textContent = '【迷雾幽林 · 荧光秘境】';
      if (biomeName === 'dungeon') banner.textContent = '【暗黑地牢 · 远古回廊】';
    }
  },

  // ---------------- BATTLE TRIGGER ----------------
  startBattle(enemyName) {
    this.closeAllModals();
    this.switchScreen('battle');
    BattleEngine.start(enemyName);
  }
};

window.addEventListener('DOMContentLoaded', () => {
  App.init();
});
