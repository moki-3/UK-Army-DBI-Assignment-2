
package com.example.dbiukarmy;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class MilitaryDBGraph extends Application {

    /* ---------- Datenbank‑Konfiguration ---------- */
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_NAME;
    private static String    DB_PORT;

    /** Wird von außen aufgerufen, bevor die Anwendung startet. */
    public static void setDatabaseConfig(String user,
                                         String password,
                                         String dbName,
                                         String    port) {
        DB_USER     = user;
        DB_PASSWORD = password;
        DB_NAME     = dbName;
        DB_PORT     = port;
    }

    /** Baut die JDBC‑URL für PostgreSQL. */
    private String getDatabaseUrl() {
        return "jdbc:postgresql://localhost:" + DB_PORT + "/" + DB_NAME;
    }

    /* ---------- UI‑Elemente ---------- */
    private Pane orgChartPane;
    private Map<Integer, RankNode> rankNodeMap = new HashMap<>();

    /* ---------- Daten‑Modelle ---------- */
    /** Enthält die Spalten aus der SQL‑Abfrage. */
    static class RankData {
        final int   rankId;
        final String rankName;
        final String abbreviation;
        final String branchName;
        final String levelName;
        final String natoCode;
        final Integer supervisorId;   // kann null sein

        RankData(int rankId, String rankName, String abbreviation,
                 String branchName, String levelName,
                 String natoCode, Integer supervisorId) {
            this.rankId      = rankId;
            this.rankName    = rankName;
            this.abbreviation= abbreviation;
            this.branchName  = branchName;
            this.levelName   = levelName;
            this.natoCode    = natoCode;
            this.supervisorId= supervisorId;
        }
    }

    /** Visual‑Node + Repräsentation der Hierarchie. */
    static class RankNode {
        final RankData data;
        final VBox visualNode;
        double x, y;                       // Position im Layout
        final List<RankNode> subordinates = new ArrayList<>();

        RankNode(RankData data) {
            this.data       = data;
            this.visualNode = createVisualNode(data);
        }

        private static VBox createVisualNode(RankData d) {
            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color:#2c3e50;"
                    + "-fx-border-color:#34495e;-fx-border-width:2;"
                    + "-fx-background-radius:5;-fx-border-radius:5;");

            Text rankText  = new Text(d.abbreviation + " - " + d.rankName);
            rankText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            rankText.setFill(Color.WHITE);

            Text natoText  = new Text("NATO: " + d.natoCode);
            natoText.setFont(Font.font("Arial", 11));
            natoText.setFill(Color.LIGHTGRAY);

            Text levelText = new Text(d.levelName);
            levelText.setFont(Font.font("Arial", 10));
            levelText.setFill(Color.LIGHTBLUE);

            box.getChildren().addAll(rankText, natoText, levelText);
            return box;
        }
    }

    /* ---------- JavaFX‑Startmethodik ---------- */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        /* Organigramm‑Bereich */
        orgChartPane = new Pane();
        orgChartPane.setStyle("-fx-background-color:#ecf0f1;");
        orgChartPane.setMinSize(1200, 800);

        ScrollPane scrollPane = new ScrollPane(orgChartPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        /* Button‑Bar */
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color:#34495e;");

        Button pngButton = new Button("Als PNG speichern");
        pngButton.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;"
                + "-fx-font-size:14;-fx-padding:10 20;");
        pngButton.setOnAction(e -> saveAsPNG(primaryStage));

        Button svgButton = new Button("Als SVG speichern");
        svgButton.setStyle("-fx-background-color:#2ecc71;-fx-text-fill:white;"
                + "-fx-font-size:14;-fx-padding:10 20;");
        svgButton.setOnAction(e -> saveAsSVG(primaryStage));

        Button reload = new Button("Reload");
        reload.setStyle("-fx-background-color:#F2CA44;-fx-text-fill:white;"
                + "-fx-font-size:14;-fx-padding:10 20;");
        reload.setOnAction(e -> {
            List<RankData> updatedRanks = loadRanksFromDatabase();
            buildOrgChart(updatedRanks);
            adjustStageSize(primaryStage);
        });

        Button newConn = new Button("Neue Verbindung");
        newConn.setStyle("-fx-background-color:#6FC9B2;-fx-text-fill:white;"
                + "-fx-font-size:14;-fx-padding:10 20;");
        newConn.setOnAction(e -> {
            primaryStage.close();
            MilitaryOrgChartLauncher launcher = new MilitaryOrgChartLauncher();
            launcher.start(new Stage());
        });

        buttonBar.getChildren().addAll(pngButton, svgButton, reload,  newConn);

        root.setCenter(scrollPane);
        root.setBottom(buttonBar);

        /* Daten laden und Diagramm bauen */
        loadDataAndCreateChart();
        adjustStageSize(primaryStage);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Militär‑Organigramm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /* ---------- Daten laden & Diagramm bauen ---------- */
    private void loadDataAndCreateChart() {
        List<RankData> ranks = loadRanksFromDatabase();

        if (ranks.isEmpty()) {
            // Wenn die DB leer ist, gibt es kein Diagramm – wir melden einen Fehler
            System.err.println("❌ Keine Daten in der Datenbank gefunden.");
            return;
        }

        buildOrgChart(ranks);
    }

    /* ---------- SQL‑Abfrage ---------- */
    private List<RankData> loadRanksFromDatabase() {
        List<RankData> ranks = new ArrayList<>();

        String sql =
                "SELECT mr.RankID, mr.RankName, mr.RankAbbreviation, "
                        + "       b.BranchName, rl.LevelName, nc.Code AS NATOCode, mr.SupervisorID "
                        + "FROM   MilitaryRanks mr "
                        + "LEFT JOIN Branches b ON mr.BranchID = b.BranchID "
                        + "LEFT JOIN RankLevels rl ON mr.RankLevelID = rl.RankLevelID "
                        + "LEFT JOIN NATOCodes nc ON mr.NATOCodeID = nc.NATOCodeID "
                        + "ORDER BY mr.RankID";

        try (Connection conn = DriverManager.getConnection(
                getDatabaseUrl(), DB_USER, DB_PASSWORD);
             Statement stmt   = conn.createStatement();
             ResultSet rs     = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int   rankId      = rs.getInt("RankID");
                String rankName    = rs.getString("RankName");
                String abbreviation= rs.getString("RankAbbreviation");
                String branchName  = rs.getString("BranchName");
                String levelName   = rs.getString("LevelName");
                String natoCode    = rs.getString("NATOCode");
                System.out.println(rankId + "\n" + rankName + "\n" + abbreviation
                + "\n" + branchName + "\n" + levelName + "\n" + natoCode  + "\n");

                Integer supervisorId =
                        rs.getObject("SupervisorID", Integer.class); // null möglich

                ranks.add(new RankData(rankId, rankName, abbreviation,
                        branchName, levelName,
                        natoCode, supervisorId));
            }

            System.out.println("✓ Ränge geladen: " + ranks.size());

        } catch (SQLException e) {
            System.err.println("❌ Datenbankfehler: " + e.getMessage());
        }

        return ranks;
    }

    /* ---------- Organigramm‑Logik ---------- */
    private void buildOrgChart(List<RankData> ranks) {

        // 1. Knoten erzeugen
        rankNodeMap.clear();
        for (RankData rd : ranks) {
            rankNodeMap.put(rd.rankId, new RankNode(rd));
        }

        // 2. Hierarchie aufbauen
        List<RankNode> roots = new ArrayList<>();

        for (RankData rd : ranks) {
            RankNode node = rankNodeMap.get(rd.rankId);

            if (rd.supervisorId == null) {
                // mehrere Roots unterstützen
                roots.add(node);
            } else {
                RankNode sup = rankNodeMap.get(rd.supervisorId);
                if (sup != null) {
                    sup.subordinates.add(node);
                } else {
                    // Supervisor existiert nicht – verwaiste Knoten → ebenfalls Root
                    roots.add(node);
                }
            }
        }

        // Wenn wirklich GAR kein Root existiert → alle als Root interpretieren
        if (roots.isEmpty()) {
            roots.addAll(rankNodeMap.values());
        }

        // 3. Layout für mehrere Roots nebeneinander
        double x = 300;  // Startpunkt
        double spacingBetweenTrees = 500;

        for (RankNode r : roots) {
            calculatePositions(r, x, 60, 450);
            x += spacingBetweenTrees;
        }

        // 4. Visualisierung
        orgChartPane.getChildren().clear();
        for (RankNode r : roots) {
            drawNode(r);
        }
    }


    private double calculatePositions(RankNode node, double x, double y, double hSpacing) {
        node.x = x;
        node.y = y;

        if (node.subordinates.isEmpty()) {
            return x;
        }

        double nextY = y + 150;
        double totalWidth = hSpacing * (node.subordinates.size() - 1);
        double startX = x - totalWidth / 2;

        for (int i = 0; i < node.subordinates.size(); i++) {
            RankNode child = node.subordinates.get(i);
            double childX = startX + i * hSpacing;
            calculatePositions(child, childX, nextY, hSpacing * 0.8);
        }

        return x;
    }


    private void drawChart(RankNode root) {
        orgChartPane.getChildren().clear();
        drawNode(root);
    }

    private void drawNode(RankNode node) {
        /* Linien zu Untergebenen */
        for (RankNode child : node.subordinates) {
            Line line = new Line(node.x, node.y + 40,
                    child.x, child.y - 10);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            orgChartPane.getChildren().add(line);

            drawNode(child);                // rekursiv
        }

        /* Knoten‑Box */
        node.visualNode.setLayoutX(node.x - 75);
        node.visualNode.setLayoutY(node.y - 30);
        node.visualNode.setPrefWidth(150);

        orgChartPane.getChildren().add(node.visualNode);
    }

    private double padding = 40;

    private void adjustStageSize(Stage stage) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (RankNode node : rankNodeMap.values()) {
            double left = node.x - 75;
            double right = node.x + 75;
            double top = node.y - 30;
            double bottom = node.y + 40;

            if (left < minX) minX = left;
            if (right > maxX) maxX = right;
            if (top < minY) minY = top;
            if (bottom > maxY) maxY = bottom;
        }

        double padding = 40;
        double width = maxX - minX + 2 * padding;
        double height = maxY - minY + 2 * padding;

        width = Math.max(width, 1000);
        height = Math.max(height, 700);

        orgChartPane.setMinSize(width, height);
        orgChartPane.setPrefSize(width, height);

        // Nur sizeToScene aufrufen, ohne Scene neu zu setzen
        stage.sizeToScene();
    }





    /* ---------- Export‑Funktionen ---------- */
    private void saveAsPNG(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("PNG speichern");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Dateien", "*.png"));
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                WritableImage img = orgChartPane.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
                System.out.println("✓ PNG gespeichert");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveAsSVG(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("SVG speichern");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SVG Dateien", "*.svg"));
        File file = fc.showSaveDialog(stage);

        if (file != null) {
            try {
                double minX = Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;
                double maxX = Double.MIN_VALUE;
                double maxY = Double.MIN_VALUE;

                for (RankNode node : rankNodeMap.values()) {
                    double left = node.x - 75;
                    double right = node.x + 75;
                    double top = node.y - 30;
                    double bottom = node.y + 40; // Höhe Box + Abstand für Linien

                    if (left < minX) minX = left;
                    if (right > maxX) maxX = right;
                    if (top < minY) minY = top;
                    if (bottom > maxY) maxY = bottom;
                }

                double padding = 20;
                minX -= padding;
                minY -= padding;
                maxX += padding;
                maxY += padding;
                double width = maxX - minX;
                double height = maxY - minY;

                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ")
                        .append("width=\"").append(width).append("\" ")
                        .append("height=\"").append(height).append("\" ")
                        .append("viewBox=\"").append(minX).append(" ")
                        .append(minY).append(" ").append(width).append(" ").append(height).append("\">\n");
                sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#ecf0f1\"/>\n");

                generateSVGContent(sb);

                sb.append("</svg>");

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(sb.toString());
                }
                System.out.println("✓ SVG gespeichert");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void generateSVGContent(StringBuilder sb) {
        for (RankNode node : rankNodeMap.values()) {
            /* Linien */
            for (RankNode child : node.subordinates) {
                sb.append("<line x1=\"").append(node.x).append("\" y1=\"")
                        .append(node.y + 40).append("\" x2=\"").append(child.x)
                        .append("\" y2=\"").append(child.y - 10).append("\" ")
                        .append("stroke=\"gray\" stroke-width=\"2\"/>\n");
            }

            /* Rechteck */
            double rectX = node.x - 75;
            double rectY = node.y - 30;

            sb.append("<rect x=\"").append(rectX).append("\" y=\"")
                    .append(rectY).append("\" width=\"150\" height=\"70\" ")
                    .append("fill=\"#2c3e50\" stroke=\"#34495e\" stroke-width=\"2\" ")
                    .append("rx=\"5\"/>\n");

            /* Texte */
            sb.append("<text x=\"").append(node.x).append("\" y=\"")
                    .append(node.y - 5).append("\" fill=\"white\" ")
                    .append("font-family=\"Arial\" font-size=\"14\" font-weight=\"bold\" text-anchor=\"middle\">")
                    .append(escapeXML(node.data.abbreviation + " - " + node.data.rankName))
                    .append("</text>\n");

            sb.append("<text x=\"").append(node.x).append("\" y=\"")
                    .append(node.y + 15).append("\" fill=\"lightgray\" ")
                    .append("font-family=\"Arial\" font-size=\"11\" text-anchor=\"middle\">")
                    .append(escapeXML("NATO: " + node.data.natoCode))
                    .append("</text>\n");

            sb.append("<text x=\"").append(node.x).append("\" y=\"")
                    .append(node.y + 30).append("\" fill=\"lightblue\" ")
                    .append("font-family=\"Arial\" font-size=\"10\" text-anchor=\"middle\">")
                    .append(escapeXML(node.data.levelName))
                    .append("</text>\n");
        }
    }

    private String escapeXML(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\'", "&apos;");
    }

    /* ---------- Haupt‑Methode ---------- */
    public static void main(String[] args) {
        launch(args);
    }
}
