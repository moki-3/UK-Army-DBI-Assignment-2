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

    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_NAME;

    public static void setDatabaseConfig(String user, String password, String dbName) {
        DB_USER = user;
        DB_PASSWORD = password;
        DB_NAME = dbName;
    }

    private String getDatabaseUrl() {
        return "jdbc:postgresql://localhost:5432/" + DB_NAME;
    }

    private Pane orgChartPane;
    private Map<Integer, RankNode> rankNodeMap = new HashMap<>();

    // Datenbank-Konfiguration - HIER DEINE DATEN EINTRAGEN!
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/" +  DB_NAME;


    static class RankData {
        int rankId;
        String rankName;
        String abbreviation;
        String branchName;
        String levelName;
        String natoCode;
        Integer supervisorId;

        RankData(int rankId, String rankName, String abbreviation, String branchName,
                 String levelName, String natoCode, Integer supervisorId) {
            this.rankId = rankId;
            this.rankName = rankName;
            this.abbreviation = abbreviation;
            this.branchName = branchName;
            this.levelName = levelName;
            this.natoCode = natoCode;
            this.supervisorId = supervisorId;
        }
    }

    static class RankNode {
        RankData data;
        VBox visualNode;
        double x, y;
        List<RankNode> subordinates = new ArrayList<>();

        RankNode(RankData data) {
            this.data = data;
            this.visualNode = createVisualNode(data);
        }

        private VBox createVisualNode(RankData data) {
            VBox box = new VBox(5);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #2c3e50; -fx-border-color: #34495e; " +
                    "-fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");

            Text rankText = new Text(data.abbreviation + " - " + data.rankName);
            rankText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            rankText.setFill(Color.WHITE);

            Text natoText = new Text("NATO: " + data.natoCode);
            natoText.setFont(Font.font("Arial", 11));
            natoText.setFill(Color.LIGHTGRAY);

            Text levelText = new Text(data.levelName);
            levelText.setFont(Font.font("Arial", 10));
            levelText.setFill(Color.LIGHTBLUE);

            box.getChildren().addAll(rankText, natoText, levelText);
            return box;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Organigramm-Bereich
        orgChartPane = new Pane();
        orgChartPane.setStyle("-fx-background-color: #ecf0f1;");
        orgChartPane.setMinSize(1200, 800);

        ScrollPane scrollPane = new ScrollPane(orgChartPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Button-Leiste
        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setStyle("-fx-background-color: #34495e;");

        Button pngButton = new Button("Als PNG speichern");
        pngButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 10 20;");
        pngButton.setOnAction(e -> saveAsPNG(primaryStage));

        Button svgButton = new Button("Als SVG speichern");
        svgButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-padding: 10 20;");
        svgButton.setOnAction(e -> saveAsSVG(primaryStage));

        buttonBar.getChildren().addAll(pngButton, svgButton);

        root.setCenter(scrollPane);
        root.setBottom(buttonBar);

        // Daten laden und Organigramm erstellen
        loadDataAndCreateChart();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Militär-Organigramm");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadDataAndCreateChart() {
        List<RankData> ranks = loadRanksFromDatabase();

        // Fallback auf Beispieldaten, wenn Datenbank nicht verfügbar
        if (ranks.isEmpty()) {
            System.out.println("⚠ Keine Datenbankverbindung - verwende Beispieldaten");
            ranks = createSampleData();
        } else {
            System.out.println("✓ Daten erfolgreich aus Datenbank geladen");
        }

        buildOrgChart(ranks);
    }

    private List<RankData> loadRanksFromDatabase() {
        List<RankData> ranks = new ArrayList<>();

        String query = "SELECT " +
                "mr.RankID, " +
                "mr.RankName, " +
                "mr.RankAbbreviation, " +
                "b.BranchName, " +
                "rl.LevelName, " +
                "nc.Code AS NATOCode, " +
                "mr.SupervisorID " +
                "FROM MilitaryRanks mr " +
                "LEFT JOIN Branches b ON mr.BranchID = b.BranchID " +
                "LEFT JOIN RankLevels rl ON mr.RankLevelID = rl.RankLevelID " +
                "LEFT JOIN NATOCodes nc ON mr.NATOCodeID = nc.NATOCodeID " +
                "ORDER BY mr.RankID";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int rankId = rs.getInt("RankID");
                String rankName = rs.getString("RankName");
                String abbreviation = rs.getString("RankAbbreviation");
                String branchName = rs.getString("BranchName");
                String levelName = rs.getString("LevelName");
                String natoCode = rs.getString("NATOCode");

                // SupervisorID kann NULL sein
                Integer supervisorId = rs.getObject("SupervisorID", Integer.class);

                ranks.add(new RankData(rankId, rankName, abbreviation,
                        branchName, levelName, natoCode, supervisorId));
            }

            System.out.println("Geladen: " + ranks.size() + " Ränge aus der Datenbank");

        } catch (SQLException e) {
            System.err.println("Datenbankfehler: " + e.getMessage());
            // Kein printStackTrace, damit es sauberer aussieht
        }

        return ranks;
    }

    private List<RankData> createSampleData() {
        List<RankData> ranks = new ArrayList<>();
        ranks.add(new RankData(1, "General", "GEN", "Army", "Generalitaet", "OF-9", null));
        ranks.add(new RankData(2, "Lieutenant General", "LTG", "Army", "Generalitaet", "OF-8", 1));
        ranks.add(new RankData(3, "Major General", "MG", "Army", "Generalitaet", "OF-7", 2));
        ranks.add(new RankData(4, "Colonel", "COL", "Army", "Offizier", "OF-5", 3));
        ranks.add(new RankData(5, "Lieutenant Colonel", "LTC", "Army", "Offizier", "OF-4", 4));
        ranks.add(new RankData(6, "Major", "MAJ", "Army", "Offizier", "OF-3", 5));
        ranks.add(new RankData(7, "Captain", "CPT", "Army", "Offizier", "OF-2", 6));
        ranks.add(new RankData(8, "Lieutenant", "LT", "Army", "Offizier", "OF-1", 7));
        ranks.add(new RankData(9, "Warrant Officer", "WO", "Army", "Unteroffizier", "WO-1", 8));
        ranks.add(new RankData(10, "Sergeant", "SGT", "Army", "Unteroffizier", "OR-5", 9));
        ranks.add(new RankData(11, "Corporal", "CPL", "Army", "Unteroffizier", "OR-4", 10));
        ranks.add(new RankData(12, "Private", "PVT", "Army", "Mannschaft", "OR-1", 11));
        return ranks;
    }

    private void buildOrgChart(List<RankData> ranks) {
        // Knoten erstellen
        for (RankData rank : ranks) {
            rankNodeMap.put(rank.rankId, new RankNode(rank));
        }

        // Hierarchie aufbauen
        RankNode root = null;
        for (RankData rank : ranks) {
            RankNode node = rankNodeMap.get(rank.rankId);
            if (rank.supervisorId == null) {
                root = node;
            } else {
                RankNode supervisor = rankNodeMap.get(rank.supervisorId);
                if (supervisor != null) {
                    supervisor.subordinates.add(node);
                }
            }
        }

        if (root != null) {
            // Layout berechnen
            calculatePositions(root, 600, 50, 500);
            // Visualisieren
            drawChart(root);
        }
    }

    private double calculatePositions(RankNode node, double x, double y, double hSpacing) {
        node.x = x;
        node.y = y;

        if (node.subordinates.isEmpty()) {
            return x;
        }

        double nextY = y + 120;
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
        // Linien zu Untergebenen zeichnen
        for (RankNode child : node.subordinates) {
            Line line = new Line(node.x, node.y + 40, child.x, child.y - 10);
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            orgChartPane.getChildren().add(line);
            drawNode(child);
        }

        // Knoten zeichnen
        node.visualNode.setLayoutX(node.x - 75);
        node.visualNode.setLayoutY(node.y - 30);
        node.visualNode.setPrefWidth(150);
        orgChartPane.getChildren().add(node.visualNode);
    }

    private void saveAsPNG(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("PNG speichern");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Dateien", "*.png"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                WritableImage image = orgChartPane.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                System.out.println("PNG erfolgreich gespeichert!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveAsSVG(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("SVG speichern");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SVG Dateien", "*.svg"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                StringBuilder svg = new StringBuilder();
                svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
                svg.append("width=\"1200\" height=\"800\">\n");
                svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#ecf0f1\"/>\n");

                generateSVGContent(svg);

                svg.append("</svg>");

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(svg.toString());
                }
                System.out.println("SVG erfolgreich gespeichert!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateSVGContent(StringBuilder svg) {
        for (RankNode node : rankNodeMap.values()) {
            // Linien zeichnen
            for (RankNode child : node.subordinates) {
                double x1 = node.x;
                double y1 = node.y + 40;
                double x2 = child.x;
                double y2 = child.y - 10;

                svg.append("<line x1=\"");
                svg.append(x1);
                svg.append("\" y1=\"");
                svg.append(y1);
                svg.append("\" x2=\"");
                svg.append(x2);
                svg.append("\" y2=\"");
                svg.append(y2);
                svg.append("\" stroke=\"gray\" stroke-width=\"2\"/>\n");
            }

            // Rechteck zeichnen
            double rectX = node.x - 75;
            double rectY = node.y - 30;

            svg.append("<rect x=\"");
            svg.append(rectX);
            svg.append("\" y=\"");
            svg.append(rectY);
            svg.append("\" width=\"150\" height=\"70\" ");
            svg.append("fill=\"#2c3e50\" stroke=\"#34495e\" stroke-width=\"2\" rx=\"5\"/>\n");

            // Text zeichnen
            String rankText = escapeXML(node.data.abbreviation + " - " + node.data.rankName);
            String natoText = escapeXML("NATO: " + node.data.natoCode);
            String levelText = escapeXML(node.data.levelName);

            svg.append("<text x=\"");
            svg.append(node.x);
            svg.append("\" y=\"");
            svg.append(node.y - 5);
            svg.append("\" fill=\"white\" font-family=\"Arial\" font-size=\"14\" font-weight=\"bold\" text-anchor=\"middle\">");
            svg.append(rankText);
            svg.append("</text>\n");

            svg.append("<text x=\"");
            svg.append(node.x);
            svg.append("\" y=\"");
            svg.append(node.y + 15);
            svg.append("\" fill=\"lightgray\" font-family=\"Arial\" font-size=\"11\" text-anchor=\"middle\">");
            svg.append(natoText);
            svg.append("</text>\n");

            svg.append("<text x=\"");
            svg.append(node.x);
            svg.append("\" y=\"");
            svg.append(node.y + 30);
            svg.append("\" fill=\"lightblue\" font-family=\"Arial\" font-size=\"10\" text-anchor=\"middle\">");
            svg.append(levelText);
            svg.append("</text>\n");
        }
    }

    private String escapeXML(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}