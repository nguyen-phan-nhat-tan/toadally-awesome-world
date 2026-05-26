module toadally.awesome.world {
    requires javafx.controls;
    requires javafx.fxml;

    exports ast;
    exports ast.marker;
    exports error;
    exports io;
    exports lexer;
    exports simulation;
    exports Release;

    opens Release to javafx.fxml;
}