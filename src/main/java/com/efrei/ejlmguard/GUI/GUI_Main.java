package com.efrei.ejlmguard.GUI;


import java.io.IOException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
public class GUI_Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Button btn = new Button();iiiiiiiiiiiiiooooooooooooooooooooooooo
        /*btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler.....ActionEvent.....() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });*/
        
        //StackPane root = new StackPane();
        //root.getChildren().add(btn);
                    

        Parent root = FXMLLoader.load(getClass().getResource("/com/efrei/ejlmguard/GUI_Design.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Hello World!");
                primaryStage.setScene(scene);
                primaryStage.show();
       
    }
 
 public static void main(String[] args) {
        launch(args);
    }
}