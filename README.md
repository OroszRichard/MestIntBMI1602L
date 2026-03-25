from PIL import Image, ImageDraw, ImageFont
from pathlib import Path
import textwrap

out_dir = Path("/mnt/data")
img_path = out_dir / "connect4_gui_mockup.png"
md_path = out_dir / "connect4_bemutato.md"

# Create mock GUI image
w, h = 1400, 900
img = Image.new("RGB", (w, h), (235, 240, 248))
draw = ImageDraw.Draw(img)

# Fonts
try:
    font_title = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 38)
    font_sub = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 26)
    font_small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 22)
    font_btn = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24)
except:
    font_title = ImageFont.load_default()
    font_sub = ImageFont.load_default()
    font_small = ImageFont.load_default()
    font_btn = ImageFont.load_default()

# Window
draw.rounded_rectangle((80, 60, 1320, 840), radius=24, fill=(250, 252, 255), outline=(180, 190, 205), width=2)

# Header
draw.rounded_rectangle((110, 95, 1290, 165), radius=16, fill=(35, 74, 138))
draw.text((145, 112), "Connect 4 - GUI nézet", font=font_title, fill="white")
draw.text((960, 118), "Játékos: Richard", font=font_sub, fill="white")

# Left status panel
draw.rounded_rectangle((115, 195, 360, 800), radius=18, fill=(244, 247, 252), outline=(190, 198, 210), width=2)
draw.text((145, 225), "Játékállapot", font=font_title, fill=(35, 74, 138))
status_lines = [
    "Aktuális kör: Játékos",
    "Ellenfél: AI",
    "Nehézség: Minimax + alfa-béta",
    "Mentés: támogatott",
    "Statisztika: győzelmek tárolása",
]
y = 285
for line in status_lines:
    draw.text((145, y), f"• {line}", font=font_small, fill=(55, 60, 70))
    y += 52

# Buttons
buttons = ["Új játék", "Mentés", "Betöltés", "Kilépés"]
y = 560
for b in buttons:
    draw.rounded_rectangle((140, y, 330, y+58), radius=12, fill=(230, 236, 245), outline=(170, 180, 195), width=2)
    tw = draw.textlength(b, font=font_btn)
    draw.text((235 - tw/2, y+14), b, font=font_btn, fill=(45, 55, 70))
    y += 76

# Column buttons top
start_x = 430
for i in range(7):
    x1 = start_x + i*115
    x2 = x1 + 90
    draw.rounded_rectangle((x1, 205, x2, 255), radius=12, fill=(224, 230, 240), outline=(160, 170, 185), width=2)
    label = str(i+1)
    tw = draw.textlength(label, font=font_btn)
    draw.text((x1 + 45 - tw/2, 217), label, font=font_btn, fill=(55, 60, 70))

# Board
board_x1, board_y1 = 420, 280
board_x2, board_y2 = 1240, 760
draw.rounded_rectangle((board_x1, board_y1, board_x2, board_y2), radius=24, fill=(45, 92, 170), outline=(25, 60, 120), width=4)

rows, cols = 6, 7
cell_w = (board_x2 - board_x1 - 70) / cols
cell_h = (board_y2 - board_y1 - 60) / rows

# Some example pieces
red_positions = {(5,0), (5,1), (4,1), (5,3), (3,2)}
yellow_positions = {(5,2), (4,2), (5,4), (4,3), (5,5)}

for r in range(rows):
    for c in range(cols):
        cx = board_x1 + 35 + c*cell_w + cell_w/2
        cy = board_y1 + 30 + r*cell_h + cell_h/2
        bbox = (cx-42, cy-42, cx+42, cy+42)
        if (r,c) in red_positions:
            fill = (220, 60, 60)
        elif (r,c) in yellow_positions:
            fill = (245, 210, 40)
        else:
            fill = (235, 240, 248)
        draw.ellipse(bbox, fill=fill, outline=(25, 60, 120), width=3)

draw.text((430, 785), "Alsó állapotsor: Az AI a következő legjobb lépést számolja...", font=font_small, fill=(65, 75, 90))

img.save(img_path)

md_content = """# Connect 4 játék bemutatása

## A játék rövid ismertetése

Ez a projekt egy **Java nyelven készített Connect 4 játék**, amely grafikus felhasználói felülettel (GUI) és mesterséges intelligencia ellenféllel is rendelkezik.  
A játék célja, hogy a játékos **négy saját korongot helyezzen el egymás mellé** vízszintesen, függőlegesen vagy átlósan, még azelőtt, hogy ezt az ellenfél megtenné.

A program nemcsak az alap játékmenetet valósítja meg, hanem tartalmaz:
- grafikus kezelőfelületet,
- játékosnév-kezelést,
- mentési és betöltési lehetőséget,
- győzelmi statisztikák tárolását,
- valamint egy **AI ellenfelet**, amely döntési algoritmussal választ lépést.

---

## Főbb funkciók

### 1. Grafikus felhasználói felület
A játék nem konzolos, hanem egy könnyen átlátható **GUI felületen** keresztül használható.  
A játékos gombok segítségével tud oszlopot választani, a tábla pedig vizuálisan jeleníti meg az aktuális állást.

### 2. Játékos vs. AI mód
A felhasználó a gép ellen játszik.  
Az AI a lépéseit **Minimax algoritmus** és **alfa-béta metszés** segítségével számolja ki, így nem véletlenszerűen játszik, hanem igyekszik a lehető legjobb döntést meghozni.

### 3. Mentés és betöltés
A játékállás menthető, így a félbehagyott játszmák később folytathatók.  
Ez különösen hasznos akkor, ha a felhasználó nem tudja egy alkalommal befejezni a játékot.

### 4. Pontszámok és statisztika
A program képes eltárolni a győzelmek számát, így nyomon követhető, hogy egy adott játékos hányszor nyert.

---

## A játék működése

A játéktábla **6 sorból és 7 oszlopból** áll.  
A játékos kiválaszt egy oszlopot, majd a korong az adott oszlop legaljára kerül.  
Ezután az AI következik, amely kiszámítja a saját optimális lépését.

A játék addig tart, amíg:
- valamelyik fél ki nem alakít **4 egymás melletti korongot**, vagy
- a tábla teljesen meg nem telik.

---

## Alkalmazott technológiák

- **Java**
- **Swing GUI**
- **Minimax algoritmus**
- **Alfa-béta metszés**
- fájlalapú mentés / statisztikakezelés

---

## GUI kinézet

Az alábbi kép a játék grafikus felületének egy mintanézetét mutatja:

![A Connect 4 játék GUI felülete](connect4_gui_mockup.png)

---

## Összegzés

A projekt célja egy olyan Connect 4 játék elkészítése volt, amely nemcsak működőképes, hanem felhasználóbarát és vizuálisan is jól kezelhető.  
A grafikus felület, az AI ellenfél, a mentési lehetőség és a statisztikakezelés együtt egy összetettebb, mégis könnyen használható alkalmazást eredményez.

Ez a program jól bemutatja a **játéklogika**, a **grafikus felületkezelés** és az **algoritmikus döntéshozatal** gyakorlati alkalmazását Java környezetben.
"""

md_path.write_text(md_content, encoding="utf-8")

print(f"Kész:\n- {md_path}\n- {img_path}")
