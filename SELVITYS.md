# Tehtävä 2
Mesh-toteutus on mahdollista toteuttaa nopeasti käyttäen Javan ObjectStreameja,
jolloin verkon yli välittyy yksittäisiä serialisoituja olioita. Tämä on kuitenkin 
abstraktion luoma "illuusio", sillä TCP-socketit kuljettavat matalammalla tasolla 
pelkkää datavirtaa, eivätkä siis pysty erottamaan missä kohtaa serialisoitu olio 
päättyy ja mistä seuraava alkaa. Oletetaan olevan yksittäisiä resursseja, kuten 
serialisoituja olioita, jotka halutaan kuljettaa verkon yli, ilman olemassa olevaa 
ObjectStreamin kaltaista abstraktiota. Miten viestitte/merkkaatte vastaanottajalle
yksittäisten resurssien rajat? (Vinkki: Voitte selvittää, miten osa olemassa
olevista protokollista ratkaisee tämän ongelman.)
----

  
Yksittäisten resurssien alkua voidaan merkitä otsikolla, joka sisältää tiedon resurssin koosta. 
Vastaanottaja tunnistaa otsikosta, että uusi resurssi alkaa, 
ja sen jälkeen tietää kuinka iso pala tulevasta datavirrasta kuuluu samaan pakettiin. 
Esimerkiksi IP-protokollassa otsikkotiedot sisältävät tiedon otsikon pituudesta, sekä paketin kokonaispituudesta.


# Tehtävä 3
Kerro lyhyesti muutama esimerkkiskenaario, mitä tietoturvaan liittyviä ongelmia 
Mesh-viestien välitys epäluotettujen solmujen kautta (eli siis toisten pelaajien, 
mahdollisesti toisen ohjelmabinäärin kautta) tuottaa ja miten ongelmia olisi
mahdollista ratkaista. (Vinkki: digital signature, public key cryptography, 
Diffie–Hellman key exchange). Vastauksessa ei tarvitse keskittyä salausalgoritmien
sisäiseen toimintaan/matemaattiseen teoriaan.             
----
</br>

Digitaalisessa allekirjoittajassa yksityinen avain on vain allekirjoittajalla, mutta 
voidaan selvittää vastaako se allekirjoittajan julkista avainta. Tätä käytetään siis
varmistamaan allekirjoittajan identiteetti. Digitaalisen avaimen heikkous on siinä, että toisessa
solmussa olevat haittaohjelmat voivat altistaa esimerkiksi salausavaimet kopiomiselle.
  
Julkisen avaimen salaus tarkoittaa epäsymmetristä salausta, jossa pyritään käytettyjen avainten
salaamiseen vaativilla laskuilla. Eli toinen avain voidaan pitää yksityisenä ja toinen julkisena.
Heikkoutena on se, että avaimen käyttäjän täyttyy luottaa toiseen solmuun, joten ei voida olla varmoja onko avaimeen kajottu.
  
Diffie-Hellmann-avaintenvaihtoprotokolla on salausprotokolla, jossa kaksi osapuolta sopii yhteisestä salaisuudesta
turvattoman tietoliikkenneyhteyden ylitse. Yhteistä salausta voidaan salata perinteisillä salausmenetelmillä.
Heikkoutena on se, että vastapuolta ei todenneta, jolloin vastapuoli voi ollakkin ihan kuka tahansa
suorittamassa man-in-the-middle hyökkäystä.

