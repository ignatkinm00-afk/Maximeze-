const fs = require('fs');
const path = require('path');

const src = path.join(__dirname, '../src/styles.css');
const outDir = path.join(__dirname, '../dist');
const dest = path.join(outDir, 'styles.css');

if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

fs.copyFileSync(src, dest);
console.log('CSS copied: dist/styles.css');
