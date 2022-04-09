package application.controller;

import application.model.*;
import javafx.collections.ObservableList;
import storage.Storage;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.PortUnreachableException;
import java.net.SocketImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Controller {


    private static Controller controller;
    private Storage storage;

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    private Controller() {
        storage = Storage.getStorage();
    }

    //     Denne metode kræver at Storage constructoren ikke er privat den er kun
    //     til JUnit test
    public Controller getTestController() {
        Controller controller = new Controller();
        controller.storage = new Storage();
        return controller;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public Salg createSalg(LocalDateTime salgsTidspunkt, ArrayList<OrdreLinje> ordreLinjer, int samletKlip, double samletPris){
        Salg salg = new Salg(salgsTidspunkt, ordreLinjer, samletPris, samletKlip);
        storage.addSalg(salg);
        return salg;
    }

    public ArrayList<Salg> getSalg(){return new ArrayList<>(storage.getSalg());}

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public Udlejning createUdlejning(LocalDateTime udlejningsTidspunkt, double samletPris, String lejersNavn, ArrayList<OrdreLinje> ordrelinjer){
        Udlejning udlejning = new Udlejning(udlejningsTidspunkt,null,samletPris,lejersNavn,ordrelinjer);
        storage.addUdlejning(udlejning);
        return udlejning;
    }

    public void removeUdlejning(Udlejning udlejning){
        storage.removeUdlejning(udlejning);
    }

    public ArrayList<Udlejning> getUdlejninger(){return new ArrayList<>(storage.getUdlejninger());}

    public ArrayList<Udlejning> getAktiveUdlejninger(){
        ArrayList<Udlejning> udlejninger = new ArrayList<>();
        for (Udlejning u : storage.getUdlejninger()){
            if (u.getAfregningsTidspunkt() == null){
                udlejninger.add(u);
            }
        }
        return udlejninger;
    }

    public ArrayList<Udlejning> getAfsluttedeUdlejninger(){
        ArrayList<Udlejning> udlejninger = new ArrayList<>();
        for (Udlejning u : storage.getUdlejninger()){
            if (u.getAfregningsTidspunkt() != null){
                udlejninger.add(u);
            }
        }
        return udlejninger;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public OrdreLinje createOrdreLinje(Pris pris){
        return new OrdreLinje(pris);
    }

    public void setAntalPåOrdreLinje(OrdreLinje ordreLinje, int antal){
        ordreLinje.setAntal(antal);
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public ProduktGruppe createproduktGruppe(String produktType){
        ProduktGruppe produktGruppe = new ProduktGruppe(produktType);
        storage.addProduktGruppe(produktGruppe);
        return produktGruppe;
    }

    public ArrayList<ProduktGruppe> getProduktGrupper(){return storage.getProduktGrupper();}

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public Produkt createProdukt(String beskrivelse, ProduktGruppe produktGruppe){
        Produkt produkt = produktGruppe.createProdukt(beskrivelse);
        return produkt;
    }

    public void sletProduktgruppe(ProduktGruppe produktGruppe){
        if (storage.getProduktGrupper().contains(produktGruppe)){
            storage.removeProduktGruppe(produktGruppe);
        }
    }

    public ArrayList<Produkt> getProdukter(){
        ArrayList<Produkt> produkter = new ArrayList<>();
        for (ProduktGruppe pG : storage.getProduktGrupper()){
            for (Produkt p : pG.getProdukter()){
                produkter.add(p);
            }
        }
        return produkter;
    }

    public void sletProdukt(ProduktGruppe produktGruppe, Produkt produkt){
        produktGruppe.removeProdukt(produkt);
    }

    public Prisliste createPrisliste(String navn){
        Prisliste prisliste = new Prisliste(navn);
        storage.addPrisliste(prisliste);
        return prisliste;
    }

    public ArrayList<Prisliste> getPrislister(){return storage.getPrislister();}



    public void sletPrisliste(Prisliste prisliste){
        if (storage.getPrislister().contains(prisliste)){
            storage.removePrisliste(prisliste);
        }
    }


    public double getSamletPris(ObservableList<OrdreLinje> ordreLinjer){
        double samletPris = 0;
        for (OrdreLinje o : ordreLinjer){
            if (o.getRabatBeregning() != null){
                samletPris += o.getRabatBeregning().getRabat((o.getPris().getPris()) * o.getAntal());
            }else{
                samletPris += o.getPris().getPris() * o.getAntal();
            }
        }
        return samletPris;
    }


    public int getSamletKlip(ObservableList<OrdreLinje> ordreLinjer){
        int samletKlip = 0;

        for (OrdreLinje o : ordreLinjer){
            samletKlip += o.getPris().getKlip() * o.getAntal();

        }
        return samletKlip;
    }

    public Pris createPrisOgKlip(double pris, Produkt produkt, int klipPris,Prisliste prisliste){
        Pris prisClass = prisliste.createPrisOgKlip(pris,produkt ,klipPris);
        return prisClass;
    }

    public Pris createPris (double pris, Produkt produkt,Prisliste prisliste){
        Pris prisClass = prisliste.createPris(pris,produkt);
        return prisClass;
    }

    public void sletPrisEllerPrisOgKlip(Prisliste prisliste, Pris pris){
        prisliste.removePris(pris);
    }

    public RabatBeregning tilføjProcentRabatTilOrdrelinje(OrdreLinje ordreLinje,double rabatProcent){
        RabatBeregning procentRabat = new ProcentRabat(rabatProcent);
        ordreLinje.setRabatBeregning(procentRabat);
        return procentRabat;
    }

    public RabatBeregning tilføjFastRabatTilOrdrelinje(OrdreLinje ordreLinje,double fastRabatPris){
        RabatBeregning fastRabat = new FastRabat(fastRabatPris);
        ordreLinje.setRabatBeregning(fastRabat);
        return fastRabat;
    }

    public ArrayList<Salg> getSalgFromDato(LocalDate start, LocalDate slut){
        ArrayList<Salg> salg = new ArrayList<>();
        if (!storage.getSalg().isEmpty()){
            for (Salg s : storage.getSalg()){
                if (s.getSalgsTidspunkt().isAfter(start.atStartOfDay()) && s.getSalgsTidspunkt().isBefore(slut.atTime(23,59))){
                    salg.add(s);
                }
            }
        }
        return salg;
    }

    /*
    public static void setPrisOgKlipForProdukt(Pris pris,double varePris,int klipPris){
        pris.setPris(varePris);
        pris.setKlip(klipPris);
    }

    public static void ændreKlasseFraPrisTilPrisOgKlip(Prisliste prisliste,Pris pris,double prisPåvare,int klipPris,Produkt produkt){
        sletPrisEllerPrisOgKlip(prisliste,pris);
        prisliste.createPrisOgKlip(prisPåvare,produkt,klipPris);
    }

    public static void ændreKlasseFraPrisOgKlipTilPris(Prisliste prisliste,Pris pris,double prisPåvare,Produkt produkt){
        sletPrisEllerPrisOgKlip(prisliste,pris);
        Controller.createPris(prisPåvare,produkt,prisliste);
    }

     */



    public void initStorage(){

        // Produktgrupper:

        ProduktGruppe flaske = controller.createproduktGruppe("Flaske");
        ProduktGruppe fadoel = controller.createproduktGruppe("Fadøl");
        ProduktGruppe fustage = controller.createproduktGruppe("Fustage");
        ProduktGruppe kulsyre = controller.createproduktGruppe("Kulsyre");
        ProduktGruppe malt = controller.createproduktGruppe("Malt");
        ProduktGruppe beklaedning = controller.createproduktGruppe("Beklædning");
        ProduktGruppe anlaeg = controller.createproduktGruppe("Anlæg");
        ProduktGruppe glas = controller.createproduktGruppe("Glas");
        ProduktGruppe spiritus = controller.createproduktGruppe("Spiritus");
        ProduktGruppe sampakning = controller.createproduktGruppe("Sampakning");
        ProduktGruppe sodavand = controller.createproduktGruppe("Sodavand");
        ProduktGruppe snacks = controller.createproduktGruppe("Snacks");
        ProduktGruppe rundvisning = controller.createproduktGruppe("Rundvisning");

        // Produkter under flaske:

        Produkt klosterbryg = controller.createProdukt("Klosterbryg", flaske);
        Produkt sweetGB = controller.createProdukt("Sweet Georgia Brown", flaske);
        Produkt extraPilsner = controller.createProdukt("Extra Pilsner", flaske);
        Produkt celebration = controller.createProdukt("Celebration", flaske);
        Produkt blondie = controller.createProdukt("Blondie", flaske);
        Produkt foraarsbryg = controller.createProdukt("Forårsbryg", flaske);
        Produkt indianPA = controller.createProdukt("Indian Pale Ale", flaske);
        Produkt julebryg = controller.createProdukt("Julebryg", flaske);
        Produkt juletoenden = controller.createProdukt("Juletønden", flaske);
        Produkt oldSA = controller.createProdukt("Old Strong Ale", flaske);
        Produkt fregattenJylland = controller.createProdukt("Fregatten Jylland", flaske);
        Produkt imperialStout = controller.createProdukt("Imperial Stout", flaske);
        Produkt tribute = controller.createProdukt("Tribute", flaske);
        Produkt blackMonster = controller.createProdukt("Black Monster", flaske);

        // Produkter under fadøl:


        Produkt klosterbrygFadoel = controller.createProdukt("Klosterbryg, 40 cl", fadoel);
        Produkt jazzClassicFadoel = controller.createProdukt("Jazz Classic, 40 cl", fadoel);
        Produkt extraPilsnerFadoel = controller.createProdukt("Extra Pilsner, 40 cl", fadoel);
        Produkt celebrationFadoel = controller.createProdukt("Celebration, 40 cl", fadoel);
        Produkt blondieFadoel = controller.createProdukt("Blondie, 40 cl", fadoel);
        Produkt foraarsbrygFadoel = controller.createProdukt("Forårsbryg, 40 cl", fadoel);
        Produkt indianPAFadoel = controller.createProdukt("Indian Pale Ale, 40 cl", fadoel);
        Produkt julebrygFadoel = controller.createProdukt("Julebryg, 40 cl", fadoel);
        Produkt imperialStoutFadoel = controller.createProdukt("Imperial Stout, 40 cl", fadoel);
        Produkt specialFadoel = controller.createProdukt("Special, 40 cl", fadoel);

        // Produkter under sodavand:

        Produkt aeblebrus = controller.createProdukt("Æblebrus", sodavand);
        Produkt cola = controller.createProdukt("Cola", sodavand);
        Produkt nikoline = controller.createProdukt("Nikoline", sodavand);
        Produkt sevenUp = controller.createProdukt("7-Up", sodavand);
        Produkt vand = controller.createProdukt("Vand", sodavand);

        // Produkter under snacks:

        Produkt chips = controller.createProdukt("Chips", snacks);
        Produkt peanuts = controller.createProdukt("Peanuts", snacks);
        Produkt oelpoelser = controller.createProdukt("Ølpølser", snacks);

        // Produkter under spiritus:

        Produkt whisky = controller.createProdukt("Whisky 45%, 50 cl rør", spiritus);
        Produkt whiskyFireCL = controller.createProdukt("Whisky, 4 cl", spiritus);
        Produkt whiskyCL = controller.createProdukt("Whisky 43%, 50 cl rør", spiritus);
        Produkt egesplint = controller.createProdukt("u/ egesplint", spiritus);
        Produkt egesplintM = controller.createProdukt("m/ egesplint", spiritus);
        Produkt wGB = controller.createProdukt("2 gange Whisky, Glas + Brikker", spiritus);
        Produkt liqourOA = controller.createProdukt("Liquor of Aarhus", spiritus);
        Produkt lyngGin = controller.createProdukt("Lyng Gin, 50 cl", spiritus);
        Produkt lynGinFireCl = controller.createProdukt("Lyng Gin, 4 cl", spiritus);

        // Produkter under fustage:

        Produkt klosterbrygFustage = controller.createProdukt("Klosterbryg, 20 liter", fustage);
        Produkt jazzClassicFustage = controller.createProdukt("Jazz Classic, 25 liter", fustage);
        Produkt extraPilsnerFustage = controller.createProdukt("Extra Pilsner, 25 liter", fustage);
        Produkt celebrationFustage = controller.createProdukt("Celebration, 20 liter", fustage);
        Produkt blondieFustage = controller.createProdukt("Blondie, 25 liter", fustage);
        Produkt foraarsbrygFustage = controller.createProdukt("Forårsbryg, 20 liter", fustage);
        Produkt indianPAFustage = controller.createProdukt("Indian Pale Ale, 20 liter", fustage);
        Produkt julebrygFustage = controller.createProdukt("Julebryg, 20 liter", fustage);
        Produkt imperialStountFustage = controller.createProdukt("Imperial Stout, 20 liter", fustage);
        Produkt pant = controller.createProdukt("Pant", fustage);

        // Produkter under Kulsyre

        Produkt kulsyreSeksKg = controller.createProdukt("Kulsyre, 6 kg", kulsyre);
        Produkt kulsyrePant = controller.createProdukt("Pant", kulsyre);
        Produkt kulsyreFireKg = controller.createProdukt("Kulsyre, 4 kg", kulsyre);
        Produkt kulsyreTiKg = controller.createProdukt("Kulsyre, 10 kg", kulsyre);

        // Produkter under Malt:

        Produkt maltSaek = controller.createProdukt("Malt, 25 kg sæk", malt);

        // Produkter under Beklædning:

        Produkt tshirt = controller.createProdukt("T-Shirt", beklaedning);
        Produkt polo = controller.createProdukt("Polo", beklaedning);
        Produkt cap = controller.createProdukt("Cap", beklaedning);

        // Produkter under Anlæg:

        Produkt enHane = controller.createProdukt("1-hane", anlaeg);
        Produkt toHane = controller.createProdukt("2-hane", anlaeg);
        Produkt flereHaner = controller.createProdukt("Bar med flere haner", anlaeg);
        Produkt levering = controller.createProdukt("Levering", anlaeg);
        Produkt krus = controller.createProdukt("Krus", anlaeg);

        // Produkter under Glas:

        Produkt uanset = controller.createProdukt("Uanset Størrelse", glas);

        // Produkter under sampakning:

        Produkt gaveEt = controller.createProdukt("Gaveæske, 2 øl, 2 glas", sampakning);
        Produkt gaveTo = controller.createProdukt("Gaveæske, 4 øl", sampakning);
        Produkt traekasse = controller.createProdukt("Trækasse, 6 øl", sampakning);
        Produkt gavekurv = controller.createProdukt("Gavekurv, 6 øl, 2 glas", sampakning);
        Produkt traekasseTo = controller.createProdukt("Trækasse, 6 øl, 6 glas", sampakning);
        Produkt traekasseTre = controller.createProdukt("Trækasse, 12 øl", sampakning);
        Produkt papkasse = controller.createProdukt("Papkasse, 12 øl", sampakning);

        // Produkt under Rundvisning:

        Produkt rundvisninger = controller.createProdukt("Pr. Person dag", rundvisning);

        // Prisliste i Bar:

        Prisliste bar = controller.createPrisliste("Bar");

        // Flaske i Bar:

        bar.createPrisOgKlip(70, klosterbryg, 2);
        bar.createPrisOgKlip(70, sweetGB, 2);
        bar.createPrisOgKlip(70, extraPilsner, 2);
        bar.createPrisOgKlip(70, celebration, 2);
        bar.createPrisOgKlip(70, blondie, 2);
        bar.createPrisOgKlip(70, foraarsbryg, 2);
        bar.createPrisOgKlip(70, indianPA, 2);
        bar.createPrisOgKlip(70, julebryg, 2);
        bar.createPrisOgKlip(70, juletoenden, 2);
        bar.createPrisOgKlip(70, oldSA, 2);
        bar.createPrisOgKlip(70, fregattenJylland, 2);
        bar.createPrisOgKlip(70, imperialStout, 2);
        bar.createPrisOgKlip(70, tribute, 2);
        bar.createPrisOgKlip(100, blackMonster, 3);

        // Fadøl i Bar:

        bar.createPrisOgKlip(38, klosterbrygFadoel, 1);
        bar.createPrisOgKlip(38, jazzClassicFadoel, 1);
        bar.createPrisOgKlip(38, extraPilsnerFadoel, 1);
        bar.createPrisOgKlip(38, celebrationFadoel, 1);
        bar.createPrisOgKlip(38, blondieFadoel, 1);
        bar.createPrisOgKlip(38, foraarsbrygFadoel, 1);
        bar.createPrisOgKlip(38, indianPAFadoel, 1);
        bar.createPrisOgKlip(38, julebrygFadoel, 1);
        bar.createPrisOgKlip(38, imperialStoutFadoel, 1);
        bar.createPrisOgKlip(38, specialFadoel, 1);

        // Sodavand i Bar:

        bar.createPris(15, aeblebrus);
        bar.createPris(15, cola);
        bar.createPris(15, nikoline);
        bar.createPris(15, sevenUp);
        bar.createPris(10, vand);

        // Snacks i Bar:

        bar.createPris(10, chips);
        bar.createPris(15, peanuts);
        bar.createPrisOgKlip(30, oelpoelser, 1);

        // Spiritus i Bar:

        bar.createPris(599, whisky);
        bar.createPris(50, whiskyFireCL);
        bar.createPris(499, whiskyCL);
        bar.createPris(300, egesplint);
        bar.createPris(350, egesplintM);
        bar.createPris(80, wGB);
        bar.createPris(175, liqourOA);
        bar.createPris(350, lyngGin);
        bar.createPris(40, lynGinFireCl);

        // Kulsyre i Bar:

        bar.createPris(400, kulsyreSeksKg);
        bar.createPris(1000, kulsyrePant);

        // Beklædning i Bar:

        bar.createPris(70, tshirt);
        bar.createPris(100, polo);
        bar.createPris(30, cap);

        // Sampakninger i Bar

        bar.createPris(110, gaveEt);
        bar.createPris(140,gaveTo);
        bar.createPris(260, traekasse);
        bar.createPris(260, gavekurv);
        bar.createPris(350, traekasseTo);
        bar.createPris(410, traekasseTre);
        bar.createPris(370, papkasse);

        // Prisliste i Butik:

        Prisliste butik = controller.createPrisliste("Butik");

        // Flaske i Butik:

        butik.createPris(36, klosterbryg);
        butik.createPris(36, sweetGB);
        butik.createPris(36, extraPilsner);
        butik.createPris(36, celebration);
        butik.createPris(36, blondie);
        butik.createPris(36, foraarsbryg);
        butik.createPris(36, indianPA);
        butik.createPris(36, julebryg);
        butik.createPris(36, juletoenden);
        butik.createPris(36, oldSA);
        butik.createPris(36, fregattenJylland);
        butik.createPris(36, imperialStout);
        butik.createPris(36, tribute);
        butik.createPris(60, blackMonster);

        // Spiritus i Butik:

        butik.createPris(599, whisky);
        butik.createPris(499, whiskyCL);
        butik.createPris(300, egesplint);
        butik.createPris(350, egesplintM);
        butik.createPris(80, wGB);
        butik.createPris(175, liqourOA);
        butik.createPris(350, lyngGin);

        // Fustage i Butik:

        butik.createPris(775, klosterbrygFustage);
        butik.createPris(625, jazzClassicFustage);
        butik.createPris(575, extraPilsnerFustage);
        butik.createPris(775, celebrationFustage);
        butik.createPris(700, blondieFustage);
        butik.createPris(775, foraarsbrygFustage);
        butik.createPris(775, indianPAFustage);
        butik.createPris(775, julebrygFustage);
        butik.createPris(775, imperialStountFustage);
        butik.createPris(200, pant);

        // Kulsyre i Butik:

        butik.createPris(400, kulsyreSeksKg);
        butik.createPris(1000, kulsyrePant);

        // Malt i Butik:

        butik.createPris(300, maltSaek);

        // Beklædning i Butik:

        butik.createPris(70, tshirt);
        butik.createPris(100, polo);
        butik.createPris(30, cap);

        // Anlæg i Butik:

        butik.createPris(250, enHane);
        butik.createPris(400, toHane);
        butik.createPris(500, flereHaner);
        butik.createPris(500, levering);
        butik.createPris(60, krus);

        // Glas i Butik:

        butik.createPris(15, uanset);

        // Sampakning i Butik:

        butik.createPris(110, gaveEt);
        butik.createPris(140, gaveTo);
        butik.createPris(260, traekasse);
        butik.createPris(260, gavekurv);
        butik.createPris(350, traekasseTo);
        butik.createPris(410, traekasseTre);
        butik.createPris(370, papkasse);

        // Rundvisning i Butik:

        butik.createPris(100, rundvisninger);

    }

    public void saveStorage() {
        try (FileOutputStream fileOut = new FileOutputStream("storage.ser")) {
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(storage);
                System.out.println("Storage saved in file storage.ser.");
            }
        } catch (IOException ex) {
            System.out.println("Error saving storage object.");
            throw new RuntimeException(ex);
        }
    }

    public void loadStorage() {
        try (FileInputStream fileIn = new FileInputStream("storage.ser")) {
            try (ObjectInputStream in = new ObjectInputStream(fileIn);) {
                storage = (Storage) in.readObject();

                System.out.println("Storage loaded from file storage.ser.");
            } catch (ClassNotFoundException ex) {
                System.out.println("Error loading storage object.");
                throw new RuntimeException(ex);
            }
        } catch (IOException ex) {
            System.out.println("Error loading storage object.");
            throw new RuntimeException(ex);
        }

    }

}
