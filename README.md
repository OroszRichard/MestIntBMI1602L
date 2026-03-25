# Connect 4 játék bemutatása

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

## Állapottér példák

Az alábbi képek a játék egyes állapotait mutatják be:

![State 1](MESTINT_V2.0/Images/state1.png)
![State 2](MESTINT_V2.0/Images/state2.png)
![State 3](MESTINT_V2.0/Images/state3.png)
![State 4](MESTINT_V2.0/Images/state4.png)
![State 5](MESTINT_V2.0/Images/state5.png)

---

## Összegzés

A projekt célja egy olyan Connect 4 játék elkészítése volt, amely nemcsak működőképes, hanem felhasználóbarát és vizuálisan is jól kezelhető.  
A grafikus felület, az AI ellenfél, a mentési lehetőség és a statisztikakezelés együtt egy összetettebb, mégis könnyen használható alkalmazást eredményez.

Ez a program jól bemutatja a **játéklogika**, a **grafikus felületkezelés** és az **algoritmikus döntéshozatal** gyakorlati alkalmazását Java környezetben.
