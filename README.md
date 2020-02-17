# Assignment
This README contains only general instructions on how to run the game. The development setup, assignment, program structure and hints for completing the assignment can be found in [doc/index.md](doc/index.md) (in Finnish).

# Project description
A game where a bunch of gorillas try to kill each other by throwing bananas on time-limited turns. Last survivor is the winner.

Requires Java 11 or later. Compatible with
Eclipse, IntelliJ IDEA and VS Code with Java Extension Pack. Minor issues with Netbeans.

## Installation

Maven:

```bash
$ git clone https://gitlab.utu.fi/tech/education/distributed-systems/distributed-gorilla

$ cd distributed-gorilla

$ mvn compile package exec:java
```

SBT:

```bash
$ git clone https://gitlab.utu.fi/tech/education/distributed-systems/distributed-gorilla

$ cd distributed-gorilla

$ sbt compile run
```

## JavaFX bugs

JavaFX has serious memory leaks that might lead to a crash in just seconds.
Use the following JVM parameter to switch to software rendering pipeline that
does not have the leaks
```
-Dprism.order=sw
```

## Tehtävänanto
Tässä harjoitustyössä on kolme päätehtävää, joista ensimmäinen on ohjelmointiharjoitus ja kaksi viimeistä selvitystehtäviä. Kahteen viimeiseen vastaukset kirjoitetaan SELVITYS.md-tidostoon pääkansiossa. Selvitystehtävät kannattaa tehdä ohjelmointitehtävän jälkeen.
Tehtävät:

1. Toteuta moninpeli (ml. chat) annettuun Gorillapeliin käyttämällä Mesh-tyylistä1 verkotusta applikaatiotasolla
2. Mesh-toteutus on mahdollista toteuttaa nopeasti käyttäen Javan ObjectStreameja, jolloin verkon yli välittyy yksittäisiä serialisoituja olioita. Tämä on kuitenkin abstraktion luoma "illuusio", sillä TCP-socketit kuljettavat matalammalla tasolla pelkkää datavirtaa, eivätkä siis pysty erottamaan missä kohtaa serialisoitu olio päättyy ja mistä seuraava alkaa. Oletetaan olevan yksittäisiä resursseja, kuten serialisoituja olioita, jotka halutaan kuljettaa verkon yli, ilman olemassa olevaa ObjectStreamin kaltaista abstraktiota. Miten viestitte/merkkaatte vastaanottajalle yksittäisten resurssien rajat? (Vinkki: Voitte selvittää, miten osa olemassa olevista protokollista ratkaisee tämän ongelman.)
3. Kerro lyhyesti muutama esimerkkiskenaario, mitä tietoturvaan liittyviä ongelmia Mesh-viestien välitys epäluotettujen solmujen kautta (eli siis toisten pelaajien, mahdollisesti toisen ohjelmabinäärin kautta) tuottaa ja miten ongelmia olisi mahdollista ratkaista.2 (Vinkki: digital signature, public key cryptography, Diffie–Hellman key exchange). Vastauksessa ei tarvitse keskittyä salausalgoritmien sisäiseen toimintaan/matemaattiseen teoriaan.


## Ensimmäisen tehtävän vaiheet ja haasteet
Ensimmäinen tehtävä on tehtävistä aikaavievin ja täten pilkottu kolmeen vaiheeseen helpottamaan työssä etenemistä. Näitä vaiheita ei tarvitse palauttaa erikseen tai muutoin eritellä. Riittää, että lopullinen ohjelma palautetaan. Jossakin vaiheessa saattaa olla tarpeen myös muokata edellisessä vaiheessa kirjoitettua koodia, mutta tässä esitetty järjestys auttaa kuitenkin suunnittelemaan oman työn aikataulutusta.

Mesh-verkkokerroksen rakentaminen (35%)
Mesh-kerroksen tulisi olla irrallaan pelitoiminnalisuudesta (ts. sen luokkien ei tulisi riippua Gorillaluokista). Sen tehtäväksi siis jää pelkästään datan lähettäminen, välittäminen ja vastaanottaminen (ilman duplikaatteja) muilta solmuilta ("nodeilta"). Myöhemmissä vaiheessa moninpeli toteutetaan tämän Mesh-kerroksen päälle. Haasteena tässä vaiheessa on opetella luomaan ja käyttämään säikeitä sockettien ja Input- ja OutputStreamien kanssa, sekä harjoittaa säikeiden välistä synkronointia ja säieturvallisten rakenteiden käyttöä. Tämän lisäksi on päätettävä, missä muodossa Mesh-viestit välitetään verkon yli (mitä otsakedataa viesteihin liitetään) ja miten ne serialisoidaan.
Tarkemmin Mesh-verkosta, vaatimuksista sekä ehdotetusta toteutuksesta on kerrottu omalla sivullansa.

Chatin toimintaansaatto käyttäen Mesh-toteutusta (15 %)
Käytetään edellisen vaiheen Mesh-toteutusta pelin Chat-viestien välittämiseen. Haasteena on tutustua pelin ohjelmarunkoon riittävästi, jotta Mesh-palvelin saadaan käynnistettyä sekä yhdistettyä olemassa olevaan Mesh-verkkoon ja ChatMessage-oliot lähetettyä käyttöliittymää käyttäen. Myös vastaanotetut Chat-viestit tulee saada tulostettua vastaanottajan päässä. Riittää, että viestit saadaan lähetettyä kaikille (yksityisviestitukea ei tarvitse implementoida).
Ohjelman kulkua, sekä tarkemmat vaatimukset on selvitetty omalla sivullansa.

## Joulu
Joululoman alkaessa olisi hyvä, että vähintään Mesh-toteutus olisi jotakuinkin toiminnassa. Chat-toiminnallisuuden lisääminen ei tähän päälle ole aikataulullisesti iso tehtävä.

## Pelin toimintaansaatto (50 %)
Pelin loppuosan toimintaansaatto on suurin yksittäinen osakokonaisuus. Tässä laajennetaan verkkotoiminnallisuus koko peliin käyttäen hyväksi edellisssä Chatin implementoinnissa opittuja tekniikoita. Haasteena on osata välittää dataa käyttöliittymäsäikeelle säieturvallisesti ilman, että käyttöliittymäsäie jumiutuu tai tietorakenteet rikkoontuvat. On myös tärkeää selvittää, mitkä oliot tulee siirtää verkon ylitse, ja mitkä puolestaan pystytään johtamaan pienemmästä määrästä dataa.
Tarkempi ohjeistus omalla sivullansa.
Huomatkaa vielä kerran, että tämä tehtävä on melko laaja ja sisältää paljon eri osia. Tämän vuoksi on ensiarvoisen tärkeää aloittaa tehtävän teko nyt ja kysyä tarvittaessa neuvoa tai varmistusta omalle ratkaisulle harjoitustyöpajoissa!
Tämän pelin kehityksen aikana ei vahingoitettu oikeita gorilloja. Muutama koodiapina on tosin saattanut menettää yöuniaan.
 


Verkko, johon uusi jäsen voi liittyä kenen tahansa jäsenen kautta. Tästä tarkemmin Mesh-verkon toteutuksesta puhuttaessa. ↩


Mikäli kiinnostut enemmän, ks. distributed-crypto ↩

E.g.

```bash
$ java -Dprism.order=sw -jar target/distributed-gorilla-1.0.jar
```

The game will allocate significant amounts of memory. Use the following switch
to restrict the heap size to avoid wasting RAM:

```
-Xmx2000m
```

References:

* https://bugs.openjdk.java.net/browse/JDK-8092801
* https://bugs.openjdk.java.net/browse/JDK-8088396
* https://bugs.openjdk.java.net/browse/JDK-8161997
* https://bugs.openjdk.java.net/browse/JDK-8156051
* https://bugs.openjdk.java.net/browse/JDK-8161914
* https://bugs.openjdk.java.net/browse/JDK-8188094
* https://stackoverflow.com/a/41398214

## Further instructions

  * Java platform: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/jvm-platform
  * Maven: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/maven-misc
  * SBT: https://gitlab.utu.fi/soft/ftdev/wikis/tutorials/sbt-misc
  * OOMkit: https://gitlab.utu.fi/tech/education/oom/oomkit

Course related

  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-chat
  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-crypto
  * https://gitlab.utu.fi/tech/education/distributed-systems/distributed-classloader

## Screenshots

![Game](web/screenshot1.png)
