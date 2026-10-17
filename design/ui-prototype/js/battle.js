/**
 * Tactical Turn-Based Battle Engine
 * Action timeline, animations, damage text floating, combat log, and victory settlement
 */

const BattleEngine = {
  active: false,
  turn: 'player', // 'player' | 'enemy'
  
  hero: {
    name: '土豆狂战士',
    level: 3,
    hp: 120,
    maxHp: 120,
    mp: 50,
    maxMp: 50,
    atk: 35,
    def: 25,
    sprite: 'hero_berserker'
  },
  
  enemy: {
    name: '迷雾史莱姆',
    level: 2,
    hp: 160,
    maxHp: 160,
    atk: 18,
    def: 10,
    sprite: 'enemy_slime'
  },
  
  skills: [
    { name: '旋风斩', cost: 15, dmg: 65, type: 'slash', desc: '强力挥砍，造成 65 点物理伤害' },
    { name: '烈焰突刺', cost: 20, dmg: 85, type: 'fireball', desc: '炎刃贯穿，造成 85 点烈焰伤害' },
    { name: '铁壁战阵', cost: 10, dmg: 0, heal: 35, type: 'blip', desc: '恢复 35 点生命并强化防御' },
    { name: '狂暴巨力', cost: 25, dmg: 110, type: 'slash', desc: '极限一击，造成 110 点暴击物理伤害' }
  ],

  items: [
    { name: '小型生命药剂', count: 3, healHp: 60, desc: '恢复 60 点生命值' },
    { name: '小型魔力药水', count: 2, healMp: 40, desc: '恢复 40 点魔力值' },
    { name: '黄金烤土豆', count: 1, healHp: 120, healMp: 50, desc: '美味全满恢复！' }
  ],

  init() {
    this.setupEventListeners();
  },

  start(enemyName = '迷雾史莱姆') {
    this.active = true;
    this.turn = 'player';
    
    // Configure enemy data
    if (enemyName.includes('哥布林')) {
      this.enemy = { name: '哥布林斥候', level: 3, hp: 180, maxHp: 180, atk: 22, def: 12, sprite: 'enemy_goblin' };
    } else if (enemyName.includes('暗黑') || enemyName.includes('Boss')) {
      this.enemy = { name: '暗黑土豆领主', level: 10, hp: 450, maxHp: 450, atk: 40, def: 25, sprite: 'boss_dark_potato' };
    } else {
      this.enemy = { name: '迷雾史莱姆', level: 2, hp: 140, maxHp: 140, atk: 16, def: 8, sprite: 'enemy_slime' };
    }

    this.hero.hp = this.hero.maxHp;
    this.hero.mp = this.hero.maxMp;

    this.renderFighters();
    this.clearLogs();
    this.log(`遭遇了强敌【${this.enemy.name} (Lv.${this.enemy.level})】！战斗开始！`, 'system');
    this.showMainMenu();
  },

  setupEventListeners() {
    window.addEventListener('keydown', (e) => {
      if (!document.getElementById('screen-battle').classList.contains('active')) return;
      if (this.turn !== 'player') return;
      
      if (e.key === 'z' || e.key === 'Z' || e.key === '1') this.playerAttack();
      if (e.key === 'x' || e.key === 'X' || e.key === '2') this.showSkillsMenu();
      if (e.key === 'c' || e.key === 'C' || e.key === '3') this.showItemsMenu();
      if (e.key === 'v' || e.key === 'V' || e.key === '4') this.playerDefend();
    });
  },

  renderFighters() {
    // Hero status
    document.getElementById('battle-hero-name').textContent = `${this.hero.name} Lv.${this.hero.level}`;
    document.getElementById('battle-hero-hp-text').textContent = `${Math.max(0, this.hero.hp)}/${this.hero.maxHp}`;
    document.getElementById('battle-hero-hp-bar').style.width = `${(Math.max(0, this.hero.hp) / this.hero.maxHp) * 100}%`;
    document.getElementById('battle-hero-img').src = `assets/sprites/${this.hero.sprite}.png`;

    // Enemy status
    document.getElementById('battle-enemy-name').textContent = `${this.enemy.name} Lv.${this.enemy.level}`;
    document.getElementById('battle-enemy-hp-text').textContent = `${Math.max(0, this.enemy.hp)}/${this.enemy.maxHp}`;
    document.getElementById('battle-enemy-hp-bar').style.width = `${(Math.max(0, this.enemy.hp) / this.enemy.maxHp) * 100}%`;
    document.getElementById('battle-enemy-img').src = `assets/sprites/${this.enemy.sprite}.png`;

    // Timeline highlight
    const heroAvatar = document.getElementById('turn-avatar-hero');
    const enemyAvatar = document.getElementById('turn-avatar-enemy');
    if (heroAvatar && enemyAvatar) {
      if (this.turn === 'player') {
        heroAvatar.classList.add('active');
        enemyAvatar.classList.remove('active');
      } else {
        heroAvatar.classList.remove('active');
        enemyAvatar.classList.add('active');
      }
    }
  },

  showMainMenu() {
    const subdeck = document.getElementById('battle-subdeck');
    subdeck.innerHTML = `
      <div class="subdeck-title">战术指令说明 (Tactical Info)</div>
      <div style="font-size: 13px; color: #a0b0c0; line-height: 1.6; padding-top: 10px;">
        <p>• <b>[1] 普通攻击</b>: 消耗 0 MP，造成稳定物理伤害并积累怒气。</p>
        <p>• <b>[2] 战术技能</b>: 释放职业强力爆发技能或防御战技。</p>
        <p>• <b>[3] 背包道具</b>: 消耗应急治疗药剂维持健康生命线。</p>
        <p>• <b>[4] 防御/逃跑</b>: 降低下回合承受伤害 50% 或撤离战场。</p>
      </div>
    `;
  },

  showSkillsMenu() {
    AudioEngine.play('blip');
    const subdeck = document.getElementById('battle-subdeck');
    let html = `<div class="subdeck-title">选择释放技能 (MP: ${this.hero.mp}/${this.hero.maxMp})</div><div class="subdeck-list">`;
    this.skills.forEach((sk, idx) => {
      const disabled = this.hero.mp < sk.cost ? 'opacity: 0.5; pointer-events: none;' : '';
      html += `
        <div class="subdeck-item" style="${disabled}" onclick="BattleEngine.castSkill(${idx})">
          <div class="subdeck-item-name">${sk.name}</div>
          <div class="subdeck-item-cost">消耗: ${sk.cost} MP</div>
          <div class="subdeck-item-desc">${sk.desc}</div>
        </div>
      `;
    });
    html += `</div>`;
    subdeck.innerHTML = html;
  },

  showItemsMenu() {
    AudioEngine.play('blip');
    const subdeck = document.getElementById('battle-subdeck');
    let html = `<div class="subdeck-title">使用战术道具 (Combat Items)</div><div class="subdeck-list">`;
    this.items.forEach((it, idx) => {
      const disabled = it.count <= 0 ? 'opacity: 0.5; pointer-events: none;' : '';
      html += `
        <div class="subdeck-item" style="${disabled}" onclick="BattleEngine.useItem(${idx})">
          <div class="subdeck-item-name">${it.name} (×${it.count})</div>
          <div class="subdeck-item-cost" style="color:#2ecc71;">点击即时使用</div>
          <div class="subdeck-item-desc">${it.desc}</div>
        </div>
      `;
    });
    html += `</div>`;
    subdeck.innerHTML = html;
  },

  playerAttack() {
    if (this.turn !== 'player') return;
    this.turn = 'acting';
    AudioEngine.play('slash');

    const baseDmg = Math.max(10, this.hero.atk - this.enemy.def + Math.floor(Math.random() * 12));
    const isCrit = Math.random() < 0.25;
    const finalDmg = isCrit ? Math.floor(baseDmg * 1.5) : baseDmg;

    this.enemy.hp -= finalDmg;
    this.spawnDamageText(finalDmg, 'enemy', isCrit);
    this.shakeScreen();

    const critText = isCrit ? '【暴击！！】' : '';
    this.log(`土豆勇士发动【普通攻击】！${critText}对【${this.enemy.name}】造成 ${finalDmg} 点伤害！`, 'player-hit');
    this.renderFighters();

    this.checkBattleStatus();
  },

  castSkill(idx) {
    if (this.turn !== 'player') return;
    const sk = this.skills[idx];
    if (this.hero.mp < sk.cost) return;

    this.turn = 'acting';
    this.hero.mp -= sk.cost;
    AudioEngine.play(sk.type);

    if (sk.dmg > 0) {
      const dmg = Math.floor(sk.dmg * (this.hero.atk / 30) + Math.random() * 10);
      this.enemy.hp -= dmg;
      this.spawnDamageText(dmg, 'enemy', true);
      this.shakeScreen();
      this.log(`土豆勇士施展了【${sk.name}】！对【${this.enemy.name}】造成 ${dmg} 点巨额伤害！`, 'player-hit');
    }

    if (sk.heal > 0) {
      this.hero.hp = Math.min(this.hero.maxHp, this.hero.hp + sk.heal);
      this.spawnDamageText(`+${sk.heal}`, 'hero', false, true);
      this.log(`土豆勇士激活【${sk.name}】！恢复了 ${sk.heal} 点生命！`, 'system');
    }

    this.renderFighters();
    this.checkBattleStatus();
  },

  useItem(idx) {
    if (this.turn !== 'player') return;
    const it = this.items[idx];
    if (it.count <= 0) return;

    it.count--;
    this.turn = 'acting';
    AudioEngine.play('coin');

    if (it.healHp) {
      this.hero.hp = Math.min(this.hero.maxHp, this.hero.hp + it.healHp);
      this.spawnDamageText(`+${it.healHp}`, 'hero', false, true);
    }
    if (it.healMp) {
      this.hero.mp = Math.min(this.hero.maxMp, this.hero.mp + it.healMp);
    }

    this.log(`土豆勇士使用了【${it.name}】！恢复状态并准备迎敌。`, 'system');
    this.renderFighters();
    this.checkBattleStatus();
  },

  playerDefend() {
    if (this.turn !== 'player') return;
    this.turn = 'acting';
    AudioEngine.play('blip');
    this.log(`土豆勇士举起盾牌采取【防御姿态】，承受伤害将大幅降低！`, 'system');
    this.enemyTurn(true);
  },

  checkBattleStatus() {
    if (this.enemy.hp <= 0) {
      this.enemy.hp = 0;
      this.renderFighters();
      setTimeout(() => this.victory(), 600);
      return;
    }

    if (this.hero.hp <= 0) {
      this.hero.hp = 0;
      this.renderFighters();
      setTimeout(() => this.defeat(), 600);
      return;
    }

    setTimeout(() => this.enemyTurn(), 700);
  },

  enemyTurn(playerDefended = false) {
    this.turn = 'enemy';
    this.renderFighters();

    setTimeout(() => {
      if (this.enemy.hp <= 0) return;
      AudioEngine.play('hit');

      let rawDmg = Math.max(8, this.enemy.atk - this.hero.def + Math.floor(Math.random() * 8));
      if (playerDefended) rawDmg = Math.floor(rawDmg * 0.4);

      this.hero.hp -= rawDmg;
      this.spawnDamageText(rawDmg, 'hero', false);
      this.shakeScreen();

      this.log(`【${this.enemy.name}】发动反击撞击！土豆勇士受到 ${rawDmg} 点伤害！`, 'enemy-hit');
      this.renderFighters();

      if (this.hero.hp <= 0) {
        setTimeout(() => this.defeat(), 600);
      } else {
        this.turn = 'player';
        this.renderFighters();
        this.showMainMenu();
      }
    }, 600);
  },

  spawnDamageText(val, target, isCrit = false, isHeal = false) {
    const arena = document.getElementById('battle-arena');
    const textElem = document.createElement('div');
    textElem.className = 'combat-floating-text';
    if (isCrit) textElem.classList.add('crit');
    if (isHeal) textElem.classList.add('heal');

    textElem.textContent = val;
    if (target === 'enemy') {
      textElem.style.right = '280px';
      textElem.style.bottom = '180px';
    } else {
      textElem.style.left = '260px';
      textElem.style.bottom = '180px';
    }

    arena.appendChild(textElem);
    setTimeout(() => textElem.remove(), 1000);
  },

  shakeScreen() {
    const arena = document.getElementById('battle-arena');
    arena.classList.remove('screen-shake');
    void arena.offsetWidth; // trigger reflow
    arena.classList.add('screen-shake');
  },

  log(msg, styleClass = '') {
    const logBox = document.getElementById('battle-log-window');
    if (!logBox) return;
    const line = document.createElement('div');
    line.className = `battle-log-msg ${styleClass}`;
    line.innerHTML = msg;
    logBox.appendChild(line);
    logBox.scrollTop = logBox.scrollHeight;
  },

  clearLogs() {
    const logBox = document.getElementById('battle-log-window');
    if (logBox) logBox.innerHTML = '';
  },

  victory() {
    AudioEngine.play('fanfare');
    const modal = document.getElementById('modal-battle-victory');
    modal.classList.add('active');
  },

  defeat() {
    AudioEngine.play('hit');
    App.switchScreen('gameover');
  }
};
