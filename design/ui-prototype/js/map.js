/**
 * Interactive Overworld & Minimap Canvas Engine
 * Terraria-style roaming, depth sorting, environmental particles, and radar minimap
 */

const MapEngine = {
  canvas: null,
  ctx: null,
  minimapCanvas: null,
  minimapCtx: null,
  
  biome: 'village', // 'village' | 'forest' | 'dungeon'
  
  player: {
    x: 400,
    y: 500,
    targetX: 400,
    targetY: 500,
    speed: 4,
    direction: 1, // 1: right, -1: left
    isMoving: false,
    walkFrame: 0,
    heroClass: 'berserker'
  },
  
  entities: [],
  particles: [],
  
  images: {},
  running: false,
  
  init() {
    this.canvas = document.getElementById('map-canvas');
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d');
    this.ctx.imageSmoothingEnabled = false;

    this.minimapCanvas = document.getElementById('minimap-canvas');
    if (this.minimapCanvas) {
      this.minimapCtx = this.minimapCanvas.getContext('2d');
    }

    this.preloadImages();
    this.setupBiome(this.biome);
    this.setupInput();
    this.startLoop();
  },

  preloadImages() {
    const list = [
      ['bg_village', 'assets/backgrounds/bg_village.png'],
      ['bg_forest', 'assets/backgrounds/bg_mist_forest.png'],
      ['bg_dungeon', 'assets/backgrounds/bg_dungeon.png'],
      ['hero_berserker', 'assets/sprites/hero_berserker.png'],
      ['hero_mage', 'assets/sprites/hero_mage.png'],
      ['hero_ranger', 'assets/sprites/hero_ranger.png'],
      ['enemy_slime', 'assets/sprites/enemy_slime.png'],
      ['enemy_goblin', 'assets/sprites/enemy_goblin.png'],
      ['npc_thomas', 'assets/sprites/npc_thomas.png'],
      ['npc_raine', 'assets/sprites/npc_raine.png']
    ];
    list.forEach(([key, src]) => {
      const img = new Image();
      img.src = src;
      this.images[key] = img;
    });
  },

  setupBiome(name) {
    this.biome = name;
    this.particles = [];
    
    if (name === 'village') {
      this.entities = [
        { type: 'npc', name: '村长 托马斯', x: 280, y: 510, sprite: 'npc_thomas', prompt: '按 E 对话' },
        { type: 'npc', name: '铁匠 雷恩', x: 680, y: 505, sprite: 'npc_raine', prompt: '按 E 打造' },
        { type: 'monster', name: '迷雾史莱姆', x: 1020, y: 520, sprite: 'enemy_slime', prompt: '遭遇战 (战斗)' }
      ];
    } else if (name === 'forest') {
      this.entities = [
        { type: 'monster', name: '迷雾史莱姆', x: 320, y: 520, sprite: 'enemy_slime', prompt: '遭遇战 (战斗)' },
        { type: 'monster', name: '哥布林斥候', x: 820, y: 505, sprite: 'enemy_goblin', prompt: '遭遇战 (战斗)' },
        { type: 'npc', name: '神秘向导', x: 1050, y: 510, sprite: 'npc_thomas', prompt: '探寻秘境' }
      ];
    } else if (name === 'dungeon') {
      this.entities = [
        { type: 'monster', name: '暗影守卫', x: 420, y: 515, sprite: 'enemy_goblin', prompt: '遭遇战 (战斗)' },
        { type: 'monster', name: '深渊史莱姆', x: 880, y: 525, sprite: 'enemy_slime', prompt: '遭遇战 (战斗)' }
      ];
    }
  },

  setupInput() {
    window.addEventListener('keydown', (e) => {
      if (!document.getElementById('screen-overworld').classList.contains('active')) return;
      if (['w', 'W', 'ArrowUp'].includes(e.key)) this.player.targetY = Math.max(480, this.player.y - 30);
      if (['s', 'S', 'ArrowDown'].includes(e.key)) this.player.targetY = Math.min(610, this.player.y + 30);
      if (['a', 'A', 'ArrowLeft'].includes(e.key)) {
        this.player.targetX = Math.max(60, this.player.x - 35);
        this.player.direction = -1;
      }
      if (['d', 'D', 'ArrowRight'].includes(e.key)) {
        this.player.targetX = Math.min(1200, this.player.x + 35);
        this.player.direction = 1;
      }
      if (e.key === 'e' || e.key === 'E' || e.key === ' ') {
        this.checkInteract();
      }
    });

    this.canvas.addEventListener('mousedown', (e) => {
      const rect = this.canvas.getBoundingClientRect();
      const clickX = e.clientX - rect.left;
      const clickY = e.clientY - rect.top;
      
      // Allow moving in the walkable ground zone (y: 470 to 620)
      if (clickY >= 460 && clickY <= 640) {
        this.player.targetX = Math.max(60, Math.min(1200, clickX));
        this.player.targetY = Math.max(480, Math.min(610, clickY));
        this.player.direction = (clickX >= this.player.x) ? 1 : -1;
      }
    });
  },

  checkInteract() {
    for (const ent of this.entities) {
      const dist = Math.hypot(ent.x - this.player.x, ent.y - this.player.y);
      if (dist < 80) {
        if (ent.type === 'npc') {
          App.openDialogue(ent.name);
        } else if (ent.type === 'monster') {
          App.startBattle(ent.name);
        }
        return;
      }
    }
  },

  startLoop() {
    this.running = true;
    const loop = () => {
      this.update();
      this.render();
      this.renderMinimap();
      if (this.running) requestAnimationFrame(loop);
    };
    requestAnimationFrame(loop);
  },

  update() {
    // Player movement interpolation
    const dx = this.player.targetX - this.player.x;
    const dy = this.player.targetY - this.player.y;
    const dist = Math.hypot(dx, dy);

    if (dist > 3) {
      this.player.isMoving = true;
      this.player.x += (dx / dist) * this.player.speed;
      this.player.y += (dy / dist) * this.player.speed;
      this.player.walkFrame += 0.2;
    } else {
      this.player.isMoving = false;
    }

    // Weather particles
    if (this.particles.length < 35) {
      if (this.biome === 'village') {
        // Falling leaves & pollen
        this.particles.push({
          x: Math.random() * 1280,
          y: -10,
          vx: Math.random() * 1.5 + 0.5,
          vy: Math.random() * 1.5 + 1.0,
          size: Math.random() * 3 + 2,
          color: Math.random() > 0.5 ? '#82d93e' : '#ffd264'
        });
      } else if (this.biome === 'forest') {
        // Glowing bioluminescent spores
        this.particles.push({
          x: Math.random() * 1280,
          y: 720 + 10,
          vx: (Math.random() - 0.5) * 1.0,
          vy: -(Math.random() * 1.2 + 0.6),
          size: Math.random() * 4 + 2,
          color: Math.random() > 0.5 ? '#5dade2' : '#9b59b6'
        });
      } else if (this.biome === 'dungeon') {
        // Fiery embers & smoke
        this.particles.push({
          x: Math.random() * 1280,
          y: 650,
          vx: (Math.random() - 0.5) * 0.8,
          vy: -(Math.random() * 1.8 + 1.0),
          size: Math.random() * 3 + 1,
          color: Math.random() > 0.5 ? '#e74c3c' : '#f39c12'
        });
      }
    }

    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;
      if (p.y > 730 || p.y < -20 || p.x > 1300 || p.x < -20) {
        this.particles.splice(i, 1);
      }
    }

    // Check interaction prompt visibility
    let nearbyPrompt = null;
    for (const ent of this.entities) {
      const dist = Math.hypot(ent.x - this.player.x, ent.y - this.player.y);
      if (dist < 75) {
        nearbyPrompt = `[E / 点击] ${ent.prompt} (${ent.name})`;
        break;
      }
    }
    const promptElem = document.getElementById('interact-prompt');
    if (promptElem) {
      if (nearbyPrompt) {
        promptElem.textContent = nearbyPrompt;
        promptElem.style.display = 'block';
      } else {
        promptElem.style.display = 'none';
      }
    }
  },

  render() {
    if (!this.ctx) return;
    this.ctx.clearRect(0, 0, 1280, 720);

    // 1. Draw Biome Background
    let bgKey = 'bg_village';
    if (this.biome === 'forest') bgKey = 'bg_forest';
    if (this.biome === 'dungeon') bgKey = 'bg_dungeon';
    
    if (this.images[bgKey] && this.images[bgKey].complete) {
      this.ctx.drawImage(this.images[bgKey], 0, 0, 1280, 720);
    }

    // 2. Depth sorting: Render entities and player sorted by Y coordinate
    const renderList = [...this.entities, { type: 'player', x: this.player.x, y: this.player.y }];
    renderList.sort((a, b) => a.y - b.y);

    for (const item of renderList) {
      if (item.type === 'player') {
        this.renderPlayer();
      } else {
        this.renderEntity(item);
      }
    }

    // 3. Environmental Particles
    for (const p of this.particles) {
      this.ctx.fillStyle = p.color;
      this.ctx.fillRect(p.x, p.y, p.size, p.size);
    }
  },

  renderPlayer() {
    const heroKey = `hero_${this.player.heroClass}`;
    const img = this.images[heroKey];
    const px = this.player.x;
    const py = this.player.y;

    // Shadow
    this.ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
    this.ctx.beginPath();
    this.ctx.ellipse(px, py + 38, 26, 8, 0, 0, Math.PI * 2);
    this.ctx.fill();

    // Bobbing / Walk animation
    const bob = this.player.isMoving ? Math.sin(this.player.walkFrame) * 4 : Math.sin(Date.now() * 0.003) * 2;

    if (img && img.complete) {
      this.ctx.save();
      this.ctx.translate(px, py + bob);
      if (this.player.direction < 0) {
        this.ctx.scale(-1, 1);
      }
      this.ctx.drawImage(img, -48, -48, 96, 96);
      this.ctx.restore();
    }

    // Player Name Tag
    this.ctx.fillStyle = '#ffd264';
    this.ctx.font = '12px var(--pixel-font)';
    this.ctx.textAlign = 'center';
    this.ctx.fillText('土豆勇士 Lv.3', px, py - 46);
  },

  renderEntity(ent) {
    const img = this.images[ent.sprite];
    const ex = ent.x;
    const ey = ent.y;

    // Entity shadow
    this.ctx.fillStyle = 'rgba(0, 0, 0, 0.4)';
    this.ctx.beginPath();
    this.ctx.ellipse(ex, ey + 38, 24, 7, 0, 0, Math.PI * 2);
    this.ctx.fill();

    const bob = Math.sin(Date.now() * 0.003 + ex) * 2;

    if (img && img.complete) {
      this.ctx.drawImage(img, ex - 40, ey - 40 + bob, 80, 80);
    }

    // Name tag
    this.ctx.fillStyle = ent.type === 'npc' ? '#5dade2' : '#ff6b5b';
    this.ctx.font = '12px var(--pixel-font)';
    this.ctx.textAlign = 'center';
    this.ctx.fillText(ent.name, ex, ey - 44);
  },

  renderMinimap() {
    if (!this.minimapCtx) return;
    const mctx = this.minimapCtx;
    mctx.clearRect(0, 0, 150, 150);

    // Dark radar background
    mctx.fillStyle = '#10171e';
    mctx.fillRect(0, 0, 150, 150);

    // Terrain outline
    mctx.fillStyle = this.biome === 'village' ? '#2e4930' : (this.biome === 'forest' ? '#1f3833' : '#1e2430');
    mctx.fillRect(10, 85, 130, 50);

    // Grid lines
    mctx.strokeStyle = 'rgba(214, 168, 72, 0.2)';
    mctx.lineWidth = 1;
    mctx.beginPath();
    mctx.arc(75, 75, 30, 0, Math.PI * 2);
    mctx.arc(75, 75, 55, 0, Math.PI * 2);
    mctx.moveTo(75, 0); mctx.lineTo(75, 150);
    mctx.moveTo(0, 75); mctx.lineTo(150, 75);
    mctx.stroke();

    // Map scaling (1280x720 -> 150x150)
    const scaleX = 130 / 1280;
    const scaleY = 130 / 720;

    // Render entity blips
    for (const ent of this.entities) {
      const mx = 10 + ent.x * scaleX;
      const my = 10 + ent.y * scaleY;
      mctx.fillStyle = ent.type === 'npc' ? '#3498db' : '#e74c3c';
      mctx.beginPath();
      mctx.arc(mx, my, 4, 0, Math.PI * 2);
      mctx.fill();
    }

    // Render player blip (pulsing green)
    const px = 10 + this.player.x * scaleX;
    const py = 10 + this.player.y * scaleY;
    const pulse = 4 + Math.sin(Date.now() * 0.008) * 1.5;

    mctx.fillStyle = '#2ecc71';
    mctx.beginPath();
    mctx.arc(px, py, pulse, 0, Math.PI * 2);
    mctx.fill();
    mctx.strokeStyle = '#fff';
    mctx.lineWidth = 1;
    mctx.stroke();
  }
};
