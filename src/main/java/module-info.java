module com.example.dbiukarmy {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.swing;
    requires org.apache.xmlgraphics.batik.svggen;
    requires org.apache.xmlgraphics.batik.dom;

    opens com.example.dbiukarmy to javafx.fxml;
    exports com.example.dbiukarmy;
}