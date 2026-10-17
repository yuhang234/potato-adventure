/**
 * Inventory & Equipment Management Module
 * 6x4 Grid, Equipment Paper Doll, Live Stats Calculation, and Rich Tooltips
 */

const InventoryEngine = {
  currentFilter: 'all',

  equipment: {
    weapon: { id: 'item_sword', name: '勇者阔剑', rarity: 'rare', type: 'weapon', slot: 'weapon', atk: 15, crit: 5, desc: '锋利耐用的精铁阔剑，镌刻着土豆王国的家徽。' },
    armor: { id: 'item_armor_iron', name: '骑士板甲', rarity: 'rare', type: 'armor', slot: 'armor', def: 25, hp: 50, desc: '厚实的精钢胸甲，能抵御强力物理冲击。' },
    accessory: { id: 'item_ring_power', name: '力量指环', rarity: 'epic', type: 'accessory', slot: 'accessory', atk: 10, spd: 5, desc: '镶嵌璀璨红宝石的古老魔戒，涌动着蛮力。' }
  },

  baseStats: {
    hp: 120,
    mp: 50,
    atk: 10,
    def: 5,
    crit: 5,
    spd: 100
  },

  bag: [
    { id: 'item_sword_epic', name: '暗黑斩魔剑', rarity: 'epic', category: 'equip', type: 'weapon', slot: 'weapon', atk: 45, crit: 15, desc: '蕴藏深渊魔能的诅咒巨刃，刀锋散发紫火。', count: 1 },
    { id: 'item_staff_fire', name: '炎火魔杖', rarity: 'rare', category: 'equip', type: 'weapon', slot: 'weapon', atk: 28, mp: 30, desc: '法师学徒的最爱，能汇聚火元素形成爆鸣。', count: 1 },
    { id: 'item_bow', name: '猎手紫杉弓', rarity: 'uncommon', category: 'equip', type: 'weapon', slot: 'weapon', atk: 18, spd: 8, desc: '由深林古木削磨而成的硬弓，拉力惊人。', count: 1 },
    { id: 'item_armor_robe', name: '秘法长袍', rarity: 'epic', category: 'equip', type: 'armor', slot: 'armor', def: 15, mp: 60, desc: '绣有群星轨迹的法师长袍，蕴含强大的魔力流动。', count: 1 },
    { id: 'item_armor_leather', name: '游侠皮甲', rarity: 'uncommon', category: 'equip', type: 'armor', slot: 'armor', def: 12, spd: 12, desc: '轻便贴身的熟牛皮轻铠，行动无声。', count: 1 },
    { id: 'item_boots_haste', name: '疾风之靴', rarity: 'rare', category: 'equip', type: 'accessory', slot: 'accessory', spd: 20, def: 5, desc: '附带轻盈羽翼的旅行靴，奔跑时带起旋风。', count: 1 },
    { id: 'item_amulet_shield', name: '远古守护坠饰', rarity: 'legendary', category: 'equip', type: 'accessory', slot: 'accessory', def: 35, hp: 100, desc: '传说中土豆先祖遗留的黄金护身符，坚不可摧。', count: 1 },
    { id: 'item_potion_hp', name: '小型生命药剂', rarity: 'common', category: 'consumable', type: 'potion', healHp: 60, desc: '恢复 60 点生命值。清甜的草药芳香。', count: 5 },
    { id: 'item_potion_mp', name: '小型魔力药水', rarity: 'common', category: 'consumable', type: 'potion', healMp: 40, desc: '恢复 40 点魔力值。闪耀微弱荧光。', count: 3 },
    { id: 'item_potato_roast', name: '黄金烤土豆', rarity: 'uncommon', category: 'consumable', type: 'food', healHp: 120, healMp: 50, desc: '外焦里嫩的烤土豆！食用后全状态充盈。', count: 2 },
    { id: 'item_slime_gel', name: '史莱姆凝胶', rarity: 'common', category: 'material', type: 'material', desc: '黏滑富有弹性的果冻状凝胶，炼金基础原料。', count: 12 },
    { id: 'item_goblin_coins', name: '哥布林铜钱袋', rarity: 'common', category: 'material', type: 'loot', desc: '从斥候身上搜缴的战利品钱袋，可找商人换金币。', count: 4 },
    { id: 'item_dark_core', name: '暗影土豆之核', rarity: 'legendary', category: 'material', type: 'boss_drop', desc: '暗黑土豆领主体内剥离的邪能晶核，剧烈脉动。', count: 1 }
  ],

  init() {
    this.render();
    this.setupTooltip();
  },

  render() {
    this.renderEquipment();
    this.renderBagGrid();
    this.updateLiveStats();
  },

  renderEquipment() {
    ['weapon', 'armor', 'accessory'].forEach(slotKey => {
      const slotElem = document.getElementById(`equip-slot-${slotKey}`);
      const item = this.equipment[slotKey];
      if (item) {
        slotElem.innerHTML = `
          <div class="doll-slot-label">${slotKey === 'weapon' ? '武器' : (slotKey === 'armor' ? '防具' : '饰品')}</div>
          <img src="assets/items/${item.id}.png" style="width:40px; height:40px;" />
        `;
        slotElem.onclick = () => this.unequip(slotKey);
        slotElem.onmouseenter = (e) => this.showTooltip(item, e);
        slotElem.onmouseleave = () => this.hideTooltip();
      } else {
        slotElem.innerHTML = `<div class="doll-slot-label">${slotKey}</div>`;
        slotElem.onclick = null;
      }
    });
  },

  renderBagGrid() {
    const grid = document.getElementById('inv-grid-container');
    grid.innerHTML = '';

    const filtered = this.bag.filter(item => {
      if (this.currentFilter === 'all') return true;
      return item.category === this.currentFilter;
    });

    for (let i = 0; i < 24; i++) {
      const item = filtered[i];
      const slotDiv = document.createElement('div');
      slotDiv.className = 'inv-slot';

      if (item) {
        slotDiv.style.backgroundImage = `url('assets/ui/slot_${item.rarity}.png')`;
        slotDiv.innerHTML = `
          <img class="inv-item-icon" src="assets/items/${item.id}.png" />
          ${item.count > 1 ? `<div class="inv-item-count">${item.count}</div>` : ''}
        `;
        slotDiv.onclick = () => this.handleSlotClick(item);
        slotDiv.onmouseenter = (e) => this.showTooltip(item, e);
        slotDiv.onmouseleave = () => this.hideTooltip();
      } else {
        slotDiv.style.backgroundImage = `url('assets/ui/slot_common.png')`;
        slotDiv.style.opacity = '0.4';
      }
      grid.appendChild(slotDiv);
    }
  },

  handleSlotClick(item) {
    if (item.category === 'equip') {
      this.equip(item);
    } else if (item.category === 'consumable') {
      AudioEngine.play('coin');
      item.count--;
      if (item.count <= 0) {
        this.bag = this.bag.filter(x => x !== item);
      }
      this.render();
      this.hideTooltip();
    }
  },

  equip(item) {
    AudioEngine.play('equip');
    const slotKey = item.slot;
    const oldEquip = this.equipment[slotKey];

    // Remove from bag
    this.bag = this.bag.filter(x => x !== item);

    // Swap old item into bag
    if (oldEquip) {
      this.bag.push(oldEquip);
    }

    // Set new equip
    this.equipment[slotKey] = item;
    this.render();
    this.hideTooltip();
  },

  unequip(slotKey) {
    const item = this.equipment[slotKey];
    if (!item) return;
    if (this.bag.length >= 24) return; // bag full

    AudioEngine.play('equip');
    this.equipment[slotKey] = null;
    this.bag.push(item);
    this.render();
    this.hideTooltip();
  },

  updateLiveStats() {
    let hpBonus = 0;
    let mpBonus = 0;
    let atkBonus = 0;
    let defBonus = 0;
    let critBonus = 0;
    let spdBonus = 0;

    Object.values(this.equipment).forEach(eq => {
      if (!eq) return;
      if (eq.hp) hpBonus += eq.hp;
      if (eq.mp) mpBonus += eq.mp;
      if (eq.atk) atkBonus += eq.atk;
      if (eq.def) defBonus += eq.def;
      if (eq.crit) critBonus += eq.crit;
      if (eq.spd) spdBonus += eq.spd;
    });

    const totalHp = this.baseStats.hp + hpBonus;
    const totalMp = this.baseStats.mp + mpBonus;
    const totalAtk = this.baseStats.atk + atkBonus;
    const totalDef = this.baseStats.def + defBonus;
    const totalCrit = this.baseStats.crit + critBonus;
    const totalSpd = this.baseStats.spd + spdBonus;

    document.getElementById('stat-hp').innerHTML = `${totalHp} <span class="stat-diff-up">(+${hpBonus})</span>`;
    document.getElementById('stat-mp').innerHTML = `${totalMp} <span class="stat-diff-up">(+${mpBonus})</span>`;
    document.getElementById('stat-atk').innerHTML = `${totalAtk} <span class="stat-diff-up">(+${atkBonus})</span>`;
    document.getElementById('stat-def').innerHTML = `${totalDef} <span class="stat-diff-up">(+${defBonus})</span>`;
    document.getElementById('stat-crit').innerHTML = `${totalCrit}% <span class="stat-diff-up">(+${critBonus}%)</span>`;
    document.getElementById('stat-spd').innerHTML = `${totalSpd} <span class="stat-diff-up">(+${spdBonus})</span>`;

    // Sync battle & overworld hero stats
    BattleEngine.hero.maxHp = totalHp;
    BattleEngine.hero.maxMp = totalMp;
    BattleEngine.hero.atk = totalAtk;
    BattleEngine.hero.def = totalDef;
  },

  setFilter(filter) {
    AudioEngine.play('blip');
    this.currentFilter = filter;
    document.querySelectorAll('.inv-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.filter === filter);
    });
    this.renderBagGrid();
  },

  setupTooltip() {
    this.tooltip = document.getElementById('inv-tooltip');
    window.addEventListener('mousemove', (e) => {
      if (this.tooltip && this.tooltip.style.display === 'block') {
        const x = Math.min(window.innerWidth - 270, e.clientX + 15);
        const y = Math.min(window.innerHeight - 200, e.clientY + 15);
        this.tooltip.style.left = `${x}px`;
        this.tooltip.style.top = `${y}px`;
      }
    });
  },

  showTooltip(item, e) {
    if (!this.tooltip) return;
    const rarityColors = {
      common: '#BDC3C7',
      uncommon: '#2ECC71',
      rare: '#3498DB',
      epic: '#9B59B6',
      legendary: '#E67E22'
    };
    const rarityNames = {
      common: '凡品',
      uncommon: '良品',
      rare: '稀有',
      epic: '史诗',
      legendary: '传说'
    };

    let statLines = [];
    if (item.atk) statLines.push(`物理攻击: +${item.atk}`);
    if (item.def) statLines.push(`物理防御: +${item.def}`);
    if (item.hp) statLines.push(`生命上限: +${item.hp}`);
    if (item.mp) statLines.push(`魔力上限: +${item.mp}`);
    if (item.crit) statLines.push(`暴击几率: +${item.crit}%`);
    if (item.spd) statLines.push(`移动速度: +${item.spd}`);
    if (item.healHp) statLines.push(`恢复生命: +${item.healHp}`);
    if (item.healMp) statLines.push(`恢复魔力: +${item.healMp}`);

    this.tooltip.innerHTML = `
      <div class="tooltip-name" style="color:${rarityColors[item.rarity]};">【${rarityNames[item.rarity]}】${item.name}</div>
      <div class="tooltip-type">类型: ${item.type.toUpperCase()} | 需求等级: Lv.3</div>
      <div class="tooltip-stats">${statLines.join('<br>')}</div>
      <div class="tooltip-desc">${item.desc}</div>
    `;
    this.tooltip.style.display = 'block';
  },

  hideTooltip() {
    if (this.tooltip) this.tooltip.style.display = 'none';
  }
};
