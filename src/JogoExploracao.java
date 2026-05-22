import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class JogoExploracao extends JPanel implements KeyListener {

	
	ArrayList<Rectangle> paredes =
		    new ArrayList<>();
	//posicao no mundo
	
    int mundoX = 0;
    int mundoY = 0;

    // mapa
    Image imagemMapa;

    // spritesheet
    Image spriteSheet;

    // tamanho de cada frame
    final int TAMANHO = 32;

    // direção atual
    int direcao = 0;

    // frame atual da animação
    int frame = 0;

    // contador para controlar velocidade da animação
    int contadorAnimacao = 0;
    

    public JogoExploracao() {

        imagemMapa = new ImageIcon("img/quarto-clara-prototipo.png").getImage();

        // spritesheet 128x128
        spriteSheet = new ImageIcon("img/personagem.png").getImage();
        
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
        
        g.setColor(Color.RED);


        // coordenadas do frame na spritesheet
        int sx1 = frame * TAMANHO;
        int sy1 = direcao * TAMANHO;

        int sx2 = sx1 + TAMANHO;
        int sy2 = sy1 + TAMANHO;

        // desenha personagem centralizado
        g.drawImage(
            spriteSheet,

            // posição na tela
            centroX - 28,
            centroY - 28,
            centroX + 28,
            centroY + 28,

            // recorte da spritesheet
            sx1,
            sy1,
            sx2,
            sy2,

            this
        );
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int tecla = e.getKeyCode();

        boolean movendo = false;

        // DIREITA
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) {

            int novoX = mundoX + 5;

            Rectangle futuro =
                new Rectangle(novoX, mundoY, 32, 32);

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

            Rectangle futuro =
                new Rectangle(novoX, mundoY, 32, 32);

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

            Rectangle futuro =
                new Rectangle(mundoX, novoY, 32, 32);

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

            Rectangle futuro =
                new Rectangle(mundoX, novoY, 32, 32);

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
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        // volta para frame parado
        frame = 0;

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
