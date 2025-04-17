<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.TextField?>
<?import javafx.scene.layout.AnchorPane?>
<?import javafx.scene.text.Font?>
<?import javafx.scene.text.Text?>
<?import javafx.scene.control.PasswordField?>

<AnchorPane prefHeight="450.0" prefWidth="600.0" xmlns="http://javafx.com/javafx/23.0.1" xmlns:fx="http://javafx.com/fxml/1" fx:controller="controllers.ModifierUserController" style="-fx-background-color: #f8f4e1;">
    <children>
        <!-- Titre -->
        <Text layoutX="150.0" layoutY="40.0" strokeType="OUTSIDE" strokeWidth="0.0" text="Modifier l'utilisateur" wrappingWidth="282">
            <font>
                <Font name="Segoe UI Semibold" size="26.0" />
            </font>
        </Text>

        <!-- Champs -->
        <TextField fx:id="nomField" layoutX="215.0" layoutY="90.0" style="-fx-background-color: white; -fx-border-color: #4c8bf5; -fx-border-radius: 5;" promptText="Enter your name"/>
        <TextField fx:id="prenomField" layoutX="215.0" layoutY="136.0" style="-fx-background-color: white; -fx-border-color: #4c8bf5; -fx-border-radius: 5;" promptText="Enter your last name"/>
        <TextField fx:id="emailField" layoutX="215.0" layoutY="179.0" style="-fx-background-color: white; -fx-border-color: #4c8bf5; -fx-border-radius: 5;" promptText="Enter your email"/>
        <PasswordField fx:id="PasswordField" layoutX="215.0" layoutY="222.0" style="-fx-background-color: white; -fx-border-color: #4c8bf5; -fx-border-radius: 5;" promptText="Enter a password"/>

        <!-- Libellés -->
        <Label layoutX="99.0" layoutY="90.0" text="Name" style="-fx-font-size: 16px; -fx-text-fill: #4c8bf5; -fx-font-weight: bold;"/>
        <Label layoutX="99.0" layoutY="136.0" text="Last Name" style="-fx-font-size: 16px; -fx-text-fill: #4c8bf5; -fx-font-weight: bold;"/>
        <Label layoutX="99.0" layoutY="179.0" text="Email" style="-fx-font-size: 16px; -fx-text-fill: #4c8bf5; -fx-font-weight: bold;"/>
        <Label layoutX="99.0" layoutY="222.0" text="Password" style="-fx-font-size: 16px; -fx-text-fill: #4c8bf5; -fx-font-weight: bold;"/>

        <!-- Bouton -->
        <Button fx:id="ModifierButton" layoutX="433.0" layoutY="370.0" mnemonicParsing="false" onAction="#handleSave" text="✏ Modifier" style="-fx-background-color: #4c8bf5; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-border-radius: 5; -fx-padding: 10 20 10 20;" />

        <!-- Labels d'erreur -->
        <Label fx:id="NameErrorLabel" layoutX="420.0" layoutY="90.0" textFill="RED" />
        <Label fx:id="LastnameErrorLabel" layoutX="420.0" layoutY="136.0" textFill="RED" />
        <Label fx:id="EmailErrorLabel" layoutX="420.0" layoutY="179.0" textFill="RED" />
        <Label fx:id="PasswordErrorLabel" layoutX="420.0" layoutY="222.0" textFill="RED" />

        <!-- Success/Error Message -->
        <Label fx:id="messageLabel" layoutX="215.0" layoutY="300.0" style="-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;"/>
    </children>
</AnchorPane>
