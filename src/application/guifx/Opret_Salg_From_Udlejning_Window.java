package application.guifx;

import application.controller.Controller;
import application.model.OrdreLinje;
import application.model.Pris;
import application.model.Udlejning;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.Text;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Opret_Salg_From_Udlejning_Window extends Stage {

    private Udlejning udlejning;

    public Opret_Salg_From_Udlejning_Window(String title, Udlejning udlejning){
        this.initStyle(StageStyle.UTILITY);
        this.initModality(Modality.APPLICATION_MODAL);
        this.setResizable(false);

        this.udlejning = udlejning;


        this.setTitle(title);
        GridPane pane = new GridPane();
        this.initContent(pane);

        Scene scene = new Scene(pane);
        this.setScene(scene);
    }

    private ListView<OrdreLinje> lvwOrdrelinjer;
    private TextField txfSamletPris, txfReturBeloeb;
    private double returBeloeb = 0;


    private void initContent(GridPane pane) {
        pane.setPadding(new Insets(10));
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setGridLinesVisible(false);


        lvwOrdrelinjer = new ListView<>();
        lvwOrdrelinjer.getItems().setAll(udlejning.getOrdrelinjer());
        pane.add(lvwOrdrelinjer,0,0);

        Button btnFjernOrdreLinje = new Button("Returner vare");
        pane.add(btnFjernOrdreLinje,0,1);
        btnFjernOrdreLinje.setPrefSize(250,20);
        btnFjernOrdreLinje.setOnAction(event -> this.returnerVare());

        Label lblReturBeloeb = new Label("Returbeløb: ");
        pane.add(lblReturBeloeb,0,2);

        txfReturBeloeb = new TextField();
        pane.add(txfReturBeloeb,0,3);
        txfReturBeloeb.setPrefSize(250,20);

        Button btnAfslutUdlejning = new Button("Afslut Udlejning");
        pane.add(btnAfslutUdlejning,0,5);
        btnAfslutUdlejning.setPrefSize(250,20);
        btnAfslutUdlejning.setOnAction(event -> this.afslutUdlejning());

    }

    private void returnerVare(){
        OrdreLinje ordreLinje = lvwOrdrelinjer.getSelectionModel().getSelectedItem();
        if (ordreLinje.getAntal() > 1){
            ordreLinje.countDown();
            lvwOrdrelinjer.getItems().setAll(udlejning.getOrdrelinjer());
            lvwOrdrelinjer.getSelectionModel().select(ordreLinje);
            returBeloeb += ordreLinje.getPris().getPris();
            txfReturBeloeb.setText(returBeloeb +"");
        }else{
            lvwOrdrelinjer.getItems().remove(ordreLinje);
            returBeloeb += ordreLinje.getPris().getPris();
            txfReturBeloeb.setText(returBeloeb +"");
        }
    }

    private void afslutUdlejning(){
        ArrayList<OrdreLinje> ordreLinjer = new ArrayList<>();
        ordreLinjer.addAll(lvwOrdrelinjer.getItems());
        Controller.createSalg(LocalDateTime.now(),ordreLinjer,-1,Controller.getSamletPris(lvwOrdrelinjer.getItems()));
        close();
    }
}