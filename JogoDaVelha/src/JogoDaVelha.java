import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class JogoDaVelha extends JFrame implements ActionListener {
    private JButton[][] botoes = new JButton[3][3];
    private char[][] tabuleiro = new char[3][3]; // controle do jogo
    private char jogadorAtual = 'X';
    private boolean jogoFinalizado = false;

    private JPanel painelTabuleiro;
    private JPanel painelInfo;
    private JLabel labelStatus;
    private JLabel labelCronometro;
    private JLabel labelPlacarX;
    private JLabel labelPlacarO;
    private JButton botaoReset;
    private JButton botaoIniciar;

    private ImageIcon iconX;
    private ImageIcon iconO;

    private int placarX = 0;
    private int placarO = 0;

    private Timer cronometro;
    private int segundos = 0;

    public JogoDaVelha() {
        setTitle("Jogo da Velha - Melhorado");
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        carregarIcones();
        criarComponentes();
        criarTelaInicial();

        setVisible(true);
    }

    private void carregarIcones() {
        iconX = carregarIcone("/x.png");
        iconO = carregarIcone("/o.png");
    }

    private ImageIcon carregarIcone(String caminho) {
        URL url = getClass().getResource(caminho);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    private void criarComponentes() {
        painelTabuleiro = new JPanel(new GridLayout(3, 3));
        painelTabuleiro.setPreferredSize(new Dimension(300, 300));

        painelInfo = new JPanel();
        painelInfo.setLayout(new GridLayout(3, 2, 10, 10));
        painelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labelStatus = new JLabel("Clique em Iniciar para jogar");
        labelStatus.setHorizontalAlignment(SwingConstants.CENTER);
        labelStatus.setFont(new Font("Arial", Font.BOLD, 16));

        labelCronometro = new JLabel("Tempo: 0s");
        labelCronometro.setHorizontalAlignment(SwingConstants.CENTER);
        labelCronometro.setFont(new Font("Arial", Font.PLAIN, 14));

        labelPlacarX = new JLabel("Vitórias X: 0");
        labelPlacarX.setHorizontalAlignment(SwingConstants.CENTER);
        labelPlacarX.setFont(new Font("Arial", Font.PLAIN, 14));

        labelPlacarO = new JLabel("Vitórias O: 0");
        labelPlacarO.setHorizontalAlignment(SwingConstants.CENTER);
        labelPlacarO.setFont(new Font("Arial", Font.PLAIN, 14));

        botaoReset = new JButton("Resetar Placar");
        botaoReset.addActionListener(e -> resetarPlacar());
        botaoReset.setEnabled(false);

        botaoIniciar = new JButton("Iniciar Jogo");
        botaoIniciar.addActionListener(e -> iniciarJogo());

        painelInfo.add(labelStatus);
        painelInfo.add(labelCronometro);
        painelInfo.add(labelPlacarX);
        painelInfo.add(labelPlacarO);
        painelInfo.add(botaoIniciar);
        painelInfo.add(botaoReset);

        add(painelTabuleiro, BorderLayout.CENTER);
        add(painelInfo, BorderLayout.SOUTH);
    }

    private void criarTelaInicial() {
        painelTabuleiro.removeAll();
        painelTabuleiro.revalidate();
        painelTabuleiro.repaint();
    }

    private void iniciarJogo() {
        jogadorAtual = 'X';
        jogoFinalizado = false;
        segundos = 0;
        labelStatus.setText("Vez do jogador: " + jogadorAtual);
        botaoReset.setEnabled(true);

        // Inicializa tabuleiro com espaços em branco
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                tabuleiro[i][j] = ' ';

        painelTabuleiro.removeAll();
        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 3; coluna++) {
                JButton btn = new JButton();
                btn.setFont(new Font("Arial", Font.BOLD, 40));
                btn.setFocusPainted(false);
                btn.setBackground(new Color(245, 245, 245));
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                btn.addActionListener(this);
                botoes[linha][coluna] = btn;
                painelTabuleiro.add(btn);
            }
        }
        painelTabuleiro.revalidate();
        painelTabuleiro.repaint();

        if (cronometro != null) {
            cronometro.cancel();
        }
        cronometro = new Timer();
        cronometro.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                segundos++;
                SwingUtilities.invokeLater(() -> labelCronometro.setText("Tempo: " + segundos + "s"));
            }
        }, 1000, 1000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jogoFinalizado) return;

        JButton botaoClicado = (JButton) e.getSource();

        // Descobre a posição do botão clicado no tabuleiro
        int linha = -1, coluna = -1;
        outer:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (botoes[i][j] == botaoClicado) {
                    linha = i;
                    coluna = j;
                    break outer;
                }
            }
        }

        if (linha == -1 || coluna == -1) return; // não encontrou

        if (tabuleiro[linha][coluna] != ' ') return; // posição já ocupada

        // Atualiza a matriz lógica
        tabuleiro[linha][coluna] = jogadorAtual;

        // Atualiza o botão visualmente
        if (jogadorAtual == 'X' && iconX != null) {
            botaoClicado.setIcon(iconX);
        } else if (jogadorAtual == 'O' && iconO != null) {
            botaoClicado.setIcon(iconO);
        } else {
            botaoClicado.setText(String.valueOf(jogadorAtual));
        }

        if (verificarVitoria()) {
            labelStatus.setText("Jogador " + jogadorAtual + " venceu!");
            jogoFinalizado = true;
            atualizarPlacar(jogadorAtual);
            pararCronometro();
            return;
        } else if (tabuleiroCompleto()) {
            labelStatus.setText("Empate!");
            jogoFinalizado = true;
            pararCronometro();
            return;
        } else {
            jogadorAtual = (jogadorAtual == 'X') ? 'O' : 'X';
            labelStatus.setText("Vez do jogador: " + jogadorAtual);
        }
    }

    private void pararCronometro() {
        if (cronometro != null) {
            cronometro.cancel();
        }
    }

    private void atualizarPlacar(char vencedor) {
        if (vencedor == 'X') {
            placarX++;
            labelPlacarX.setText("Vitórias X: " + placarX);
        } else if (vencedor == 'O') {
            placarO++;
            labelPlacarO.setText("Vitórias O: " + placarO);
        }
    }

    private void resetarPlacar() {
        placarX = 0;
        placarO = 0;
        labelPlacarX.setText("Vitórias X: 0");
        labelPlacarO.setText("Vitórias O: 0");
        labelStatus.setText("Placar resetado. Clique em Iniciar para jogar.");
        jogoFinalizado = true;
        pararCronometro();
        painelTabuleiro.removeAll();
        painelTabuleiro.revalidate();
        painelTabuleiro.repaint();
        botaoReset.setEnabled(false);
    }

    private boolean tabuleiroCompleto() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (tabuleiro[i][j] == ' ') return false;
        return true;
    }

    private boolean verificarVitoria() {
        // Linhas
        for (int i = 0; i < 3; i++)
            if (tabuleiro[i][0] == jogadorAtual && tabuleiro[i][1] == jogadorAtual && tabuleiro[i][2] == jogadorAtual)
                return true;

        // Colunas
        for (int i = 0; i < 3; i++)
            if (tabuleiro[0][i] == jogadorAtual && tabuleiro[1][i] == jogadorAtual && tabuleiro[2][i] == jogadorAtual)
                return true;

        // Diagonais
        if (tabuleiro[0][0] == jogadorAtual && tabuleiro[1][1] == jogadorAtual && tabuleiro[2][2] == jogadorAtual)
            return true;

        if (tabuleiro[0][2] == jogadorAtual && tabuleiro[1][1] == jogadorAtual && tabuleiro[2][0] == jogadorAtual)
            return true;

        return false;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Falha ao aplicar Nimbus");
        }
        SwingUtilities.invokeLater(JogoDaVelha::new);
    }
}
