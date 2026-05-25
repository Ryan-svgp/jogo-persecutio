import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class JogoExploracao extends JPanel implements KeyListener {

    ArrayList<Rectangle> paredes = new ArrayList<>();
    
    //posicao no mundo
    int mundoX = 0;
    int mundoY = 0;

    // mapa e spritesheet
    Image imagemMapa;
    Image spriteSheet;
    Image luzMapa;

    // tamanho de cada frame
    final int TAMANHO = 32;

    // direção atual
    int direcao = 0;

    // frame atual da animação
    int frame = 0;

    // contador para controlar velocidade da animação
    int contadorAnimacao = 0;
    
 // som dos passos
    Clip somPassos;
    boolean andando = false;
    Clip ambiente;

    // ==========================================
    // 1. VARIÁVEIS DA PAUSA
    // ==========================================
    boolean pausado = false;
    int opcao = 0;

    public JogoExploracao() {
        imagemMapa = new ImageIcon("img/quarto-clara-prototipo.png").getImage();
        spriteSheet = new ImageIcon("img/personagem.png").getImage();
        luzMapa = new ImageIcon("img/luz-sombra-temp.png").getImage();

        paredes.add(new Rectangle(0, 0, 528, 48));
        paredes.add(new Rectangle(0, 0, 48, 480));
        paredes.add(new Rectangle(0, 432, 336, 48));
        paredes.add(new Rectangle(432, 432, 96, 48));
        paredes.add(new Rectangle(480, 0, 48, 480));
        paredes.add(new Rectangle(338, 0, 20, 240));
        paredes.add(new Rectangle(336, 196, 48, 48));
        paredes.add(new Rectangle(460, 196, 48, 48));
        
        mundoX = imagemMapa.getWidth(this) / 2;
        mundoY = imagemMapa.getHeight(this) / 2;

        try {

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(
                    new File("audio/passos.wav")
                );

            somPassos = AudioSystem.getClip();

            somPassos.open(audio);

        } catch (Exception e) {

            e.printStackTrace();

        }
        
        try {

            AudioInputStream audio =
                AudioSystem.getAudioInputStream(
                    new File("audio/ambiente.wav")
                );

            ambiente = AudioSystem.getClip();

            ambiente.open(audio);
            
            FloatControl volume =
            	    (FloatControl)
            	    ambiente.getControl(FloatControl.Type.MASTER_GAIN);

            	volume.setValue(-15.0f);
            	
            	 ambiente.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {

            e.printStackTrace();

        }
        
       
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight()); 
        
        // centro REAL da tela
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;

        // câmera
        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;
        
        
        g.setColor(Color.RED);
        for (Rectangle parede : paredes) {
            g.drawRect(
                parede.x + cameraX,
                parede.y + cameraY,
                parede.width,
                parede.height
            );
        }

        // mapa
        g.drawImage(imagemMapa, cameraX, cameraY, this);

        // coordenadas do frame na spritesheet
        int sx1 = frame * TAMANHO;
        int sy1 = direcao * TAMANHO;
        int sx2 = sx1 + TAMANHO;
        int sy2 = sy1 + TAMANHO;

        // desenha personagem centralizado
        g.drawImage(
            spriteSheet,
            centroX - 28,
            centroY - 28,
            centroX + 28,
            centroY + 28,
            sx1,
            sy1,
            sx2,
            sy2,
            this
        );
        
        g.drawImage(luzMapa, cameraX, cameraY, this);

        // ==========================================
        // 2. DESENHO DO MENU DE PAUSA
        // ==========================================
        if (pausado) {
            // Escurece um pouco a tela
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Desenha os textos bem no meio da tela
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            
            // O "? :" é um truque do Java que escreve a seta só na opção selecionada
            g.drawString(opcao == 0 ? "-> VOLTAR" : "   VOLTAR", centroX - 100, centroY);
            g.drawString(opcao == 1 ? "-> SAIR" : "   SAIR", centroX - 100, centroY + 60);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        // ==========================================
        // 3. CONTROLES DA PAUSA
        // ==========================================
        // Se apertar ESC, pausa/despausa e trava o código aqui com o "return"
        if (tecla == KeyEvent.VK_ESCAPE) { pausado = !pausado; repaint(); return; }
        
        // Se estiver pausado, controla o menu e não deixa o boneco andar
        if (pausado) {
            if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) opcao = 0;
            if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) opcao = 1;
            if (tecla == KeyEvent.VK_ENTER && opcao == 0) pausado = false;
            if (tecla == KeyEvent.VK_ENTER && opcao == 1) System.exit(0); // Fecha o jogo
            repaint();
            return; 
        }

        // --- CÓDIGO ORIGINAL DE ANDAR (Só roda se NÃO estiver pausado) ---
        boolean movendo = false;

        // DIREITA
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) {
            int novoX = mundoX + 5;
            Rectangle futuro = new Rectangle(novoX, mundoY, 32, 32);
            boolean bateu = false;
            for (Rectangle parede : paredes) {
                if (futuro.intersects(parede)) {
                    bateu = true;
                }
            }
            if (!bateu) {
                mundoX = novoX;
            }
            direcao = 1;
            movendo = true;
        }

        // ESQUERDA
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) {
            int novoX = mundoX - 5;
            Rectangle futuro = new Rectangle(novoX, mundoY, 32, 32);
            boolean bateu = false;
            for (Rectangle parede : paredes) {
                if (futuro.intersects(parede)) {
                    bateu = true;
                }
            }
            if (!bateu) {
                mundoX = novoX;
            }
            direcao = 2;
            movendo = true;
        }

        // CIMA
        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) {
            int novoY = mundoY - 5;
            Rectangle futuro = new Rectangle(mundoX, novoY, 32, 32);
            boolean bateu = false;
            for (Rectangle parede : paredes) {
                if (futuro.intersects(parede)) {
                    bateu = true;
                }
            }
            if (!bateu) {
                mundoY = novoY;
            }
            direcao = 3;
            movendo = true;
        }

        // BAIXO
        if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) {
            int novoY = mundoY + 5;
            Rectangle futuro = new Rectangle(mundoX, novoY, 32, 32);
            boolean bateu = false;
            for (Rectangle parede : paredes) {
                if (futuro.intersects(parede)) {
                    bateu = true;
                }
            }
            if (!bateu) {
                mundoY = novoY;
            }
            direcao = 0;
            movendo = true;
        }

        // animação
        if (movendo) {

            contadorAnimacao++;

            if (contadorAnimacao >= 5) {

                contadorAnimacao = 0;

                frame++;

                if (frame > 3) {

                    frame = 0;

                }
            }

            // toca passos
            if (!andando) {

                somPassos.setFramePosition(0);

                somPassos.loop(Clip.LOOP_CONTINUOUSLY);

                andando = true;

            }
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (pausado) return;
        frame = 0;

     // para o som
     somPassos.stop();

     andando = false;

     repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
