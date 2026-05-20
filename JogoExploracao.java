import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class JogoExploracao extends JPanel implements KeyListener {
    // Posição inicial do personagem
    int mundoX = 1000;
    int mundoY = 1000;
    int velocidade = 10; // Velocidade de movimento

    Image imagemPersonagem;
    Image imagemMapa;

    // Estados do jogo
    boolean mundoUmbra = false;
    boolean puzzleResolvido = false;

    // Sistema de Colisão (Hitboxes)
    ArrayList<Rectangle> paredes = new ArrayList<>();
    Rectangle zonaPuzzle;

    public JogoExploracao() {
        imagemMapa = new ImageIcon("img/repouso.jpg").getImage();
        imagemPersonagem = new ImageIcon("img/nave.png").getImage();

        addKeyListener(this);
        setFocusable(true);

        // --- DEFINIÇÃO DE COLISÕES ---
        // Você precisará ajustar estes retângulos (x, y, largura, altura)
        // para baterem exatamente com o desenho das paredes no 'repouso.jpg'

        // Exemplo: Bloqueando a parede da Recepção
        paredes.add(new Rectangle(600, 100, 400, 50)); // Parede superior da recepção

        // Área do primeiro Puzzle (Ex: um arquivo na sala do zelador ou enfermaria)
        zonaPuzzle = new Rectangle(800, 800, 100, 100);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Pega o centro exato da tela atual do monitor
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;

        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;

        // Fundo preto para as áreas fora do mapa
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 1º Desenha o mapa base (Tamanho estimado 2000x1500, ajuste se a imagem for maior)
        int larguraMapa = 2000;
        int alturaMapa = 1500;
        g.drawImage(imagemMapa, cameraX, cameraY, larguraMapa, alturaMapa, this);

        // 2º Desenha a zona do puzzle (Somente para você ver onde está. Apague depois!)
        if (!puzzleResolvido) {
            g.setColor(new Color(255, 255, 0, 150)); // Amarelo transparente
            g.fillRect(cameraX + zonaPuzzle.x, cameraY + zonaPuzzle.y, zonaPuzzle.width, zonaPuzzle.height);
        }

        // 3º Desenha as paredes de colisão (Apenas para debug! Apague o bloco abaixo na versão final)
        g.setColor(new Color(0, 0, 255, 100)); // Azul transparente
        for (Rectangle parede : paredes) {
            g.fillRect(cameraX + parede.x, cameraY + parede.y, parede.width, parede.height);
        }

        // 4º Desenha o personagem (sempre fixo no centro da tela)
        g.drawImage(imagemPersonagem, centroX - 25, centroY - 25, 50, 50, this);

        // 5º Aplica o Efeito do Mundo Umbra por cima de tudo
        if (mundoUmbra) {
            g.setColor(new Color(100, 0, 0, 120)); // Filtro vermelho escuro transparente
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("MUNDO UMBRA (Sanidade caindo...)", 50, 50);
        }

        // UI com instruções
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("WASD: Mover | E: Tomar Pílula (Mundo Umbra) | F: Interagir", 50, 80);
    }

    // Função que verifica se o próximo passo vai bater em alguma coisa
    private boolean podeMover(int proxX, int proxY) {
        // Cria uma hitbox provisória onde o personagem QUER ir
        Rectangle hitboxJogador = new Rectangle(proxX - 25, proxY - 25, 50, 50);

        // Limites externos do mapa para não andar no vácuo preto (Ajuste o 2000 e 1500)
        if (proxX < 25 || proxX > 1975 || proxY < 25 || proxY > 1475) {
            return false;
        }

        // Verifica se a hitbox provisória encosta em alguma parede cadastrada
        for (Rectangle parede : paredes) {
            if (hitboxJogador.intersects(parede)) {
                return false; // Bateu na parede, bloqueia o movimento
            }
        }
        return true; // Caminho livre
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        int proxX = mundoX;
        int proxY = mundoY;

        // Suporte para WASD e Setinhas
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) { proxX += velocidade; }
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) { proxX -= velocidade; }
        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) { proxY -= velocidade; }
        if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) { proxY += velocidade; }

        // Transição de mundo com a Pílula
        if (tecla == KeyEvent.VK_E) {
            mundoUmbra = !mundoUmbra;
        }

        // Sistema de Interação (Puzzle)
        if (tecla == KeyEvent.VK_F) {
            Rectangle hitboxJogador = new Rectangle(mundoX - 25, mundoY - 25, 50, 50);

            // Se o jogador apertar F enquanto está em cima da área do puzzle
            if (hitboxJogador.intersects(zonaPuzzle) && !puzzleResolvido) {
                puzzleResolvido = true;
                JOptionPane.showMessageDialog(this, "Você encontrou o prontuário na ala médica!\nPrimeiro puzzle resolvido.");
            }
        }

        // Só atualiza a posição oficial se a função podeMover retornar true
        if (podeMover(proxX, proxY)) {
            mundoX = proxX;
            mundoY = proxY;
        }

        repaint(); // Atualiza a tela
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}