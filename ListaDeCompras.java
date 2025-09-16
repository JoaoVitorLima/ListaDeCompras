import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ListaDeCompras extends Application {

    private ObservableList<String> listaDeCompras;
    private ListView<String> listaVisualizavel;
    private TextField textFieldDescricaoItem;
    private Label contadorItens;
    private File arquivoLista = new File("listaDeCompras.txt");

    @Override
    public void start(Stage palco) {
        palco.setTitle("Lista de Compras - Organizador Pessoal");

        // Inicializar a lista
        listaDeCompras = FXCollections.observableArrayList();
        carregarListaSalva();

        // Componentes da interface
        textFieldDescricaoItem = new TextField();
        textFieldDescricaoItem.setPromptText("Digite um item para adicionar à lista...");
        
        Button botaoAdicionar = new Button("Adicionar");
        Button botaoRemover = new Button("Remover");
        Button botaoExportar = new Button("Exportar Lista");
        Button botaoLimpar = new Button("Limpar Tudo");
        
        contadorItens = new Label("Itens na lista: 0");
        contadorItens.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E8B57;");

        // Configurar a ListView
        listaVisualizavel = new ListView<>(listaDeCompras);
        listaVisualizavel.setPlaceholder(new Label("Sua lista de compras está vazia. Adicione alguns itens!"));
        
        // Barra de Ferramentas
        HBox painelSuperior = new HBox(10);
        painelSuperior.setPadding(new Insets(10));
        painelSuperior.getChildren().addAll(
            new Label("Item:"), textFieldDescricaoItem, botaoAdicionar
        );

        HBox painelInferior = new HBox(10);
        painelInferior.setPadding(new Insets(10));
        painelInferior.getChildren().addAll(
            botaoRemover, botaoExportar, botaoLimpar, contadorItens
        );

        // Layout principal
        BorderPane layoutPrincipal = new BorderPane();
        layoutPrincipal.setTop(painelSuperior);
        layoutPrincipal.setCenter(listaVisualizavel);
        layoutPrincipal.setBottom(painelInferior);

        // Ações dos botões
        botaoAdicionar.setOnAction(e -> adicionarItem());
        textFieldDescricaoItem.setOnAction(e -> adicionarItem());
        
        botaoRemover.setOnAction(e -> removerItem());
        botaoExportar.setOnAction(e -> exportarLista());
        botaoLimpar.setOnAction(e -> limparLista());

        // Permitir remoção com tecla Delete
        listaVisualizavel.setOnKeyPressed(e -> {
            switch(e.getCode()) {
                case DELETE:
                    removerItem();
                    break;
            }
        });

        // Atualizar contador inicial
        atualizarContador();

        Scene cena = new Scene(layoutPrincipal, 500, 400);
        palco.setScene(cena);
        palco.show();
    }

    private void adicionarItem() {
        String item = textFieldDescricaoItem.getText().trim();
        if (!item.isEmpty()) {
            listaDeCompras.add(item);
            textFieldDescricaoItem.clear();
            atualizarContador();
            salvarLista();
        } else {
            mostrarAlerta("Item vazio", "Por favor, digite um item para adicionar à lista.");
        }
    }

    private void removerItem() {
        int indiceSelecionado = listaVisualizavel.getSelectionModel().getSelectedIndex();
        if (indiceSelecionado >= 0) {
            listaDeCompras.remove(indiceSelecionado);
            atualizarContador();
            salvarLista();
        } else {
            mostrarAlerta("Nenhum item selecionado", "Por favor, selecione um item para remover.");
        }
    }

    private void exportarLista() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Lista de Compras");
            fileChooser.setInitialFileName("minha_lista_de_compras.txt");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivos de Texto", "*.txt")
            );
            
            File arquivo = fileChooser.showSaveDialog(null);
            if (arquivo != null) {
                try (PrintWriter writer = new PrintWriter(arquivo)) {
                    writer.println("=== MINHA LISTA DE COMPRAS ===");
                    writer.println("==============================");
                    for (int i = 0; i < listaDeCompras.size(); i++) {
                        writer.println((i + 1) + ". " + listaDeCompras.get(i));
                    }
                    writer.println("==============================");
                    writer.println("Total de itens: " + listaDeCompras.size());
                }
                mostrarAlerta("Sucesso", "Lista exportada com sucesso para: " + arquivo.getName());
            }
        } catch (Exception ex) {
            mostrarErro("Erro ao exportar lista", ex.getMessage());
        }
    }

    private void limparLista() {
        if (!listaDeCompras.isEmpty()) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar limpeza");
            confirmacao.setHeaderText("Limpar toda a lista?");
            confirmacao.setContentText("Esta ação não pode ser desfeita.");
            
            ButtonType sim = new ButtonType("Sim");
            ButtonType nao = new ButtonType("Não");
            confirmacao.getButtonTypes().setAll(sim, nao);
            
            confirmacao.showAndWait().ifPresent(resposta -> {
                if (resposta == sim) {
                    listaDeCompras.clear();
                    atualizarContador();
                    salvarLista();
                }
            });
        }
    }

    private void carregarListaSalva() {
        if (arquivoLista.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivoLista))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    if (!linha.trim().isEmpty()) {
                        listaDeCompras.add(linha);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Erro ao carregar lista: " + ex.getMessage());
            }
        }
    }

    private void salvarLista() {
        try (PrintWriter writer = new PrintWriter(arquivoLista)) {
            for (String item : listaDeCompras) {
                writer.println(item);
            }
        } catch (Exception ex) {
            mostrarErro("Erro ao salvar lista", ex.getMessage());
        }
    }

    private void atualizarContador() {
        contadorItens.setText("Itens na lista: " + listaDeCompras.size());
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}