import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.ArrayList;

public class JogoExploracao extends JPanel implements KeyListener {

    ArrayList<Rectangle> paredes = new ArrayList<>();

    // ==========================================
    // IMPORTAÇÃO E ENCAPSULAMENTO DO NOVO COMPONENTE
    // ==========================================

    private GerenciadorColisao sistemaColisao;

    // Posição no mundo
    int mundoX = 0;
    int mundoY = 0;

    // Mapa e spritesheet
    Image imagemMapa;
    Image spriteSheet;
    Image luzMapa;

    // Tamanho de cada frame
    final int TAMANHO = 32;

    //configurações físicas da personagem; (Novo bloco inserirdo)
    final int Largura_Hitbox = 36;
    final int Altura_Hitbox = 36;
    final int Velocidade = 6;

    // Direção atual
    int direcao = 0;

    // Frame atual da animação
    int frame = 0;

    // Contador para controlar velocidade da animação
    int contadorAnimacao = 0;

    // Som dos passos e ambiente
    Clip somPassos;
    boolean andando = false;
    Clip ambiente;

    // ==========================================
    // 1. VARIÁVEIS DA PAUSA
    // ==========================================
    boolean pausado = false;
    int opcao = 0;

    public JogoExploracao() {
        imagemMapa = new ImageIcon("img/quarto.png").getImage();
        spriteSheet = new ImageIcon("img/personagem.png").getImage();
        luzMapa = new ImageIcon("img/luz-sombra-temp.png").getImage();

        //Instanciação do sistema de colisão Geométrica (Novo)
        sistemaColisao = new GerenciadorColisao();
        int escalaJogo = 2;//voltar se nescessario

        mundoX = 75;//mudar para o que era
        mundoY = (320 * escalaJogo) - Altura_Hitbox - 20;//voltar se nescessario

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("audio/passos.wav"));
            somPassos = AudioSystem.getClip();
            somPassos.open(audio);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("audio/ambiente.wav"));
            ambiente = AudioSystem.getClip();
            ambiente.open(audio);

            FloatControl volume = (FloatControl) ambiente.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-15.0f);
            ambiente.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        addKeyListener(this);
        setFocusable(true);
    }

    //
    //Com as linhas da hitbox do mapa
    //
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Centro REAL da tela
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;

        // Câmera
        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;

        int escala = 2;

        // 1. DESENHA O MAPA
        g2d.drawImage(imagemMapa, cameraX, cameraY, imagemMapa.getWidth(this)*escala, imagemMapa.getHeight(this)*escala, this);

        // ===================================================================
        // SISTEMA VISUAL DE DEBUG (DESENHA AS CAIXAS DE COLISÃO)
        // ==========================================
        boolean modoDebug = true; // Mude para 'false' quando quiser esconder as caixas no jogo final
        
        if (modoDebug && sistemaColisao != null) {
            // Guarda as configurações originais de linha do pincel
            Stroke traçoOriginal = g2d.getStroke();
            
            // 1. Define uma linha verde um pouco mais espessa para as zonas caminháveis
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.GREEN);
            
            for (Rectangle zona : sistemaColisao.getZonasCaminhaveis()) {
                // Converte as coordenadas do mundo para a posição correta na tela baseada na câmera
                int zonaTelaX = zona.x + cameraX;
                int zonaTelaY = zona.y + cameraY;
                
                // Desenha a borda do retângulo
                g2d.drawRect(zonaTelaX, zonaTelaY, zona.width, zona.height);
            }
            
            // 2. Desenha a Hitbox atual do Personagem em Vermelho
            g2d.setColor(Color.RED);
            int jogadorTelaX = mundoX + cameraX;
            int jogadorTelaY = mundoY + cameraY;
            g2d.drawRect(jogadorTelaX, jogadorTelaY, Largura_Hitbox, Altura_Hitbox);
            
            // Restaura o traço padrão
            g2d.setStroke(traçoOriginal);
        }
        // ===================================================================

        // Coordenadas do frame na spritesheet
        int sx1 = frame * TAMANHO;
        int sy1 = direcao * TAMANHO;
        int sx2 = sx1 + TAMANHO;
        int sy2 = sy1 + TAMANHO;

        // ==========================================
        // 2. LÓGICA DO ESPELHO / REFLEXO
        // ==========================================

        int espelhoMundoX = 200; 
        int espelhoMundoY = 530; 
        int espelhoLargura = 100;
        int espelhoAltura = 45;

        int espelhoTelaX = cameraX + espelhoMundoX;
        int espelhoTelaY = cameraY + espelhoMundoY;

        g2d.setColor(new Color(100, 149, 237, 250));
        g2d.fillRect(espelhoTelaX, espelhoTelaY, espelhoLargura, espelhoAltura);

        if (mundoX + 28 > espelhoMundoX && mundoX - 28 < espelhoMundoX + espelhoLargura) {
            int basePersonagemMundoY = mundoY + 28;
            int baseEspelhoMundoY = espelhoMundoY + espelhoAltura;

            if (basePersonagemMundoY >= baseEspelhoMundoY) {
                int distanciaDoEspelho = basePersonagemMundoY - baseEspelhoMundoY;
                int baseEspelhoTelaY = espelhoTelaY + espelhoAltura;
                int reflexoTelaY_Bottom = baseEspelhoTelaY - (distanciaDoEspelho/5);
                int reflexoTelaY_Top = reflexoTelaY_Bottom - 56; 

                Shape clipOriginal = g2d.getClip();
                g2d.setClip(espelhoTelaX, espelhoTelaY, espelhoLargura, espelhoAltura);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                int direcaoReflexo = direcao;
                if (direcao == 3) direcaoReflexo = 0; 
                else if (direcao == 0) direcaoReflexo = 3;

                if (direcao == 1) direcaoReflexo = 2;
                else if (direcao == 2) direcaoReflexo = 1;

                int reflexoSy1 = direcaoReflexo * TAMANHO;
                int reflexoSy2 = reflexoSy1 + TAMANHO;

                g2d.drawImage(
                        spriteSheet,
                        centroX + 28, reflexoTelaY_Top,    
                        centroX - 28, reflexoTelaY_Bottom, 
                        sx1, reflexoSy1,                     
                        sx2, reflexoSy2,                     
                        this
                );

                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setClip(clipOriginal);
            }
        }
        // ==========================================

        // 3. DESENHA O PERSONAGEM REAL (Sempre por cima do mapa e do espelho)
        g2d.drawImage(
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

        // 4. DESENHA A LUZ/SOMBRA DO MAPA
        g2d.drawImage(luzMapa, cameraX, cameraY, luzMapa.getWidth(this)*escala, luzMapa.getHeight(this)*escala, this);

        // ==========================================
        // 5. DESENHO DO MENU DE PAUSA
        // ==========================================
        if (pausado) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));

            g2d.drawString(opcao == 0 ? "-> VOLTAR" : "   VOLTAR", centroX - 100, centroY);
            g2d.drawString(opcao == 1 ? "-> SAIR" : "   SAIR", centroX - 100, centroY + 60);
        }
    }
    
    //
    // Sem as linhas da hi8tbox do mapa 
    //
    /* 
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Centro REAL da tela
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;

        // Câmera
        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;

        int escala = 2;

        // 1. DESENHA O MAPA
        g2d.drawImage(imagemMapa, cameraX, cameraY, imagemMapa.getWidth(this)*escala, imagemMapa.getHeight(this)*escala, this);

        // Coordenadas do frame na spritesheet
        int sx1 = frame * TAMANHO;
        int sy1 = direcao * TAMANHO;
        int sx2 = sx1 + TAMANHO;
        int sy2 = sy1 + TAMANHO;

        // ==========================================
        // 2. LÓGICA DO ESPELHO / REFLEXO
        // ==========================================

        // Ajuste essas propriedades para bater exatamente com a localização do espelho no seu 'quarto.png'
        int espelhoMundoX = 200; // Eixo X do espelho no MUNDO
        int espelhoMundoY = 530; // Eixo Y do espelho no MUNDO
        int espelhoLargura = 100;
        int espelhoAltura = 45;

        // Converte as coordenadas do espelho no mundo para coordenadas de TELA (acompanhando a câmera)
        int espelhoTelaX = cameraX + espelhoMundoX;
        int espelhoTelaY = cameraY + espelhoMundoY;

        // Desenha um fundo translúcido para o espelho, para dar aparência de vidro
        g2d.setColor(new Color(100, 149, 237, 250));
        g2d.fillRect(espelhoTelaX, espelhoTelaY, espelhoLargura, espelhoAltura);

        // O boneco tem largura total de 56 (vai de centroX - 28 a centroX + 28)
        // Verifica se o eixo X do jogador (mundoX) está dentro da área horizontal do espelho
        if (mundoX + 28 > espelhoMundoX && mundoX - 28 < espelhoMundoX + espelhoLargura) {

            // Base inferior onde o personagem está pisando no mundo
            int basePersonagemMundoY = mundoY + 28;
            // Base inferior do espelho no mundo
            int baseEspelhoMundoY = espelhoMundoY + espelhoAltura;

            // Desenha o reflexo apenas se o jogador estiver "abaixo" (na frente) do espelho
            if (basePersonagemMundoY >= baseEspelhoMundoY) {

                int distanciaDoEspelho = basePersonagemMundoY - baseEspelhoMundoY;

                // Matemática do Reflexo adaptada para a Câmera:
                int baseEspelhoTelaY = espelhoTelaY + espelhoAltura;
                int reflexoTelaY_Bottom = baseEspelhoTelaY - (distanciaDoEspelho/5);
                int reflexoTelaY_Top = reflexoTelaY_Bottom - 56; // 56 é a altura do personagem (28 * 2)

                Shape clipOriginal = g2d.getClip();
                // Limita a área de desenho estritamente aos limites do espelho na tela
                g2d.setClip(espelhoTelaX, espelhoTelaY, espelhoLargura, espelhoAltura);

                // Aplica 50% de opacidade
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                // Se olha pra cima (3), o reflexo usa o sprite de olhar pra baixo (0)
                int direcaoReflexo = direcao;
                if (direcao == 3) direcaoReflexo = 0; // Frente <-> Costas
                else if (direcao == 0) direcaoReflexo = 3;

                // Inverter Esquerda <-> Direita se seu spritesheet tiver lados diferentes
                if (direcao == 1) direcaoReflexo = 2;
                else if (direcao == 2) direcaoReflexo = 1;

                int reflexoSy1 = direcaoReflexo * TAMANHO;
                int reflexoSy2 = reflexoSy1 + TAMANHO;

                //  Inversão Horizontal (Eixo X):
                // No drawImage, trocamos o destinoX1 pelo destinoX2.
                // O padrão é (X_Esquerda, Y_Topo, X_Direita, Y_Base)

                g2d.drawImage(
                        spriteSheet,
                        centroX + 28, reflexoTelaY_Top,    // Canto Superior Esquerdo de Destino (Invertido horizontalmente usando X+28)
                        centroX - 28, reflexoTelaY_Bottom, // Canto Inferior Direito de Destino (Invertido horizontalmente usando X-28)
                        sx1, reflexoSy1,                     // Topo/Esquerda da Spritesheet (Usa o novo Sy1 calculado)
                        sx2, reflexoSy2,                     // Base/Direita da Spritesheet (Usa o novo Sy2 calculado)
                        this
                );

                // Restaura as propriedades gráficas originais
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setClip(clipOriginal);
            }
        }
         
        // ==========================================

        // 3. DESENHA O PERSONAGEM REAL (Sempre por cima do mapa e do espelho)
        g2d.drawImage(
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

        // 4. DESENHA A LUZ/SOMBRA DO MAPA
        g2d.drawImage(luzMapa, cameraX, cameraY, luzMapa.getWidth(this)*escala, luzMapa.getHeight(this)*escala, this);

        // ==========================================
        // 5. DESENHO DO MENU DE PAUSA
        // ==========================================
        if (pausado) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));

            g2d.drawString(opcao == 0 ? "-> VOLTAR" : "   VOLTAR", centroX - 100, centroY);
            g2d.drawString(opcao == 1 ? "-> SAIR" : "   SAIR", centroX - 100, centroY + 60);
        }
    }
    */
    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        // Pausa
        if (tecla == KeyEvent.VK_ESCAPE) { pausado = !pausado; repaint(); return; }

        // Menu Pausa
        if (pausado) {
            if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) opcao = 0;
            if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) opcao = 1;
            if (tecla == KeyEvent.VK_ENTER && opcao == 0) pausado = false;
            if (tecla == KeyEvent.VK_ENTER && opcao == 1) System.exit(0);
            repaint();
            return;
        }

        boolean movendo = false;

        // DIREITA
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) {
            int novoX = mundoX + Velocidade;
            // Valida se a hitbox do personagem estará contida em uma zona caminhável
            if (sistemaColisao.verificarPosicaoValida(novoX, mundoY, Largura_Hitbox, Altura_Hitbox)) {
                mundoX = novoX;
            }
            direcao = 1;
            movendo = true;
        }

        // ESQUERDA
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) {
            int novoX = mundoX - Velocidade;
            // Valida se a hitbox do personagem estará contida em uma zona caminhável
            if (sistemaColisao.verificarPosicaoValida(novoX, mundoY, Largura_Hitbox, Altura_Hitbox)) {
                mundoX = novoX;
            }
            direcao = 2;
            movendo = true;
        }

        // CIMA
        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) {
            int novoY = mundoY - Velocidade;
            // Valida se a hitbox do personagem estará contida em uma zona caminhável
            if (sistemaColisao.verificarPosicaoValida(mundoX, novoY, Largura_Hitbox, Altura_Hitbox)) {
                mundoY = novoY;
            }
            direcao = 3;
            movendo = true;
        }

        // BAIXO
        if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) {
            int novoY = mundoY + Velocidade;
            // Valida se a hitbox do personagem estará contida em uma zona caminhável
            if (sistemaColisao.verificarPosicaoValida(mundoX, novoY, Largura_Hitbox, Altura_Hitbox)) {
                mundoY = novoY;
            }
            direcao = 0;
            movendo = true;
        }

        // Animação
        if (movendo) {
            contadorAnimacao++;
            if (contadorAnimacao >= 4) {
                contadorAnimacao = 0;
                frame++;
                if (frame > 3) frame = 0;
            }

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

        somPassos.stop();
        andando = false;

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}