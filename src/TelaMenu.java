import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

// Importações para o Áudio
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class TelaMenu extends JPanel implements KeyListener {

    // Nossas imagens e o GIF
    Image imagemFundo;
    Image imagemLogo;
    Image imagemVhs;

    // Opções do Menu
    String[] opcoes = {"NOVO JOGO", "SAIR"};
    int opcaoSelecionada = 0; // Começa no "NOVO JOGO"
    JFrame janelaPrincipal;

    // Toca-fitas do Java para a música
    Clip musicaFundo;

    public TelaMenu(JFrame janela) {
        this.janelaPrincipal = janela;

        // 1. Carrega todas as imagens da pasta img/
        imagemFundo = new ImageIcon("img/fundo_menu.jpg").getImage();
        imagemLogo = new ImageIcon("img/titulo_logo.png").getImage();
        imagemVhs = new ImageIcon("img/vhs.gif").getImage();

        addKeyListener(this);
        setFocusable(true);

        // 2. Toca a música assim que o menu abre
        tocarMusica("audio/musica_menu.wav");
    }

    // --- FUNÇÃO PARA TOCAR ÁUDIO ---
    public void tocarMusica(String caminhoArquivo) {
        try {
            File arquivo = new File(caminhoArquivo);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(arquivo);

            musicaFundo = AudioSystem.getClip();
            musicaFundo.open(audioStream);
            musicaFundo.loop(Clip.LOOP_CONTINUOUSLY); // Toca em loop infinito

        } catch (Exception e) {
            System.out.println("Erro ao tocar música. O arquivo .wav existe na pasta audio?");
        }
    }

    // --- ONDE DESENHAMOS A TELA ---
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1º: A foto da Maria Clara (Fundo)
        g.drawImage(imagemFundo, 0, 0, getWidth(), getHeight(), this);

        // 2º: A Logo do jogo
        g.drawImage(imagemLogo, 100, 100, 600, 150, this);

        // 3º: O Menu de opções
        g.setFont(new Font("Arial", Font.BOLD, 40));

        for (int i = 0; i < opcoes.length; i++) {
            // Se for o botão selecionado, pinta de branco e coloca a seta FIXA
            if (i == opcaoSelecionada) {
                g.setColor(Color.WHITE);
                g.drawString("-> " + opcoes[i], 100, 350 + (i * 60));
            }
            // Se não for, pinta de cinza e sem seta
            else {
                g.setColor(Color.GRAY);
                g.drawString("   " + opcoes[i], 100, 350 + (i * 60));
            }
        }

        // ==========================================
        // 4º: O GIF DO VHS (Com truque de Transparência)
        // ==========================================
        Graphics2D g2d = (Graphics2D) g;

        // Coloca 30% de opacidade no pincel
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        // Desenha o GIF esticado na tela inteira
        g2d.drawImage(imagemVhs, 0, 0, getWidth(), getHeight(), this);

        // Volta o pincel para 100% de opacidade
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // --- CONTROLES DO TECLADO ---
    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) {
            opcaoSelecionada--;
            if (opcaoSelecionada < 0) opcaoSelecionada = 2; // Volta pro final
            repaint(); // Atualiza a tela
        }
        else if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) {
            opcaoSelecionada++;
            if (opcaoSelecionada > 2) opcaoSelecionada = 0; // Volta pro topo
            repaint(); // Atualiza a tela
        }
        else if (tecla == KeyEvent.VK_ENTER) {
            if (opcaoSelecionada == 0) { // NOVO JOGO

                // Para a música antes de ir pro jogo
                if (musicaFundo != null) {
                    musicaFundo.stop();
                }

                // Troca as telas
                janelaPrincipal.getContentPane().removeAll();
                JogoExploracao jogo = new JogoExploracao();
                janelaPrincipal.add(jogo);
                janelaPrincipal.revalidate();
                janelaPrincipal.repaint();
                jogo.requestFocusInWindow();

            }else if (opcaoSelecionada == 1) { // SAIR
                System.exit(0);
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
