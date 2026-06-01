import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.*;
import java.awt.event.*;

public class JogoExploracao extends JPanel implements KeyListener {

    // Classes auxiliares criadas por vocês
    private GerenciadorColisao sistemaColisao;
    private GerenciadorDebug sistemaDebug;

    // Variáveis que controlam "em que mundo" o jogador está e o modo de teste
    boolean mundoUmbra = false;
    boolean mostrarHitboxes = false; // Começa falso para parecer um jogo de verdade. Aperte 'H' para testar.

    // 1.0f significa 100% visível. Vai diminuindo até 0.0f (invisível)
    float opacidadeTutorial = 1.0f;

    // Variáveis que controlam o enredo da história
    int missaoAtual = 1;
    int documentosAchados = 1;
    int partesObjetoCircular = 0; // Quantas peças a Clara já pegou

    // Variáveis booleanas (Verdadeiro ou Falso) que funcionam como "chaves" do jogo
    boolean portaUmbraDestrancada = false;
    boolean sabePalavraMagica = false;
    String mensagemAviso = ""; // Texto de aviso que surge embaixo do boneco

    // Controles para o jogador não conseguir pegar a mesma peça duas vezes
    boolean pegouPecaEspelho = false;
    boolean pegouPecaGaveta = false;
    boolean pegouPecaNpc = false;
    boolean leuDocumentoRecepcao = false;

    // Controlam se o jogo deve pausar a tela para mostrar uma imagem de perto (puzzle)
    boolean mostrandoPuzzlePorta = false;
    boolean mostrandoEspelho = false;
    boolean mostrandoImagemFinalNpc = false;
    boolean mostrarTextoAreaLiberada = false;

    // Definição das coordenadas (X e Y) de todos os objetos interativos do jogo
    public Rectangle areaPilula = new Rectangle(115, 560, 50, 50);
    public Rectangle areaCama = new Rectangle(50, 560, 80, 90);
    public Rectangle areaPortaUmbra = new Rectangle(390, 890, 80, 60);
    public Rectangle areaEspelho = new Rectangle(200, 530, 100, 55);
    public Rectangle areaGaveta = new Rectangle(450, 800, 50, 50);
    public Rectangle areaNPC = new Rectangle(1600, 300, 50, 50);
    public Rectangle areaDocumento = new Rectangle(1290, 200, 40, 40);

    // Física e tamanho da personagem (Maria Clara)
    public int mundoX = 0; // Posição X dela no mundo
    public int mundoY = 0; // Posição Y dela no mundo
    final int TAMANHO = 32; // Tamanho do recorte da imagem do boneco
    public final int Largura_Hitbox = 36;
    public final int Altura_Hitbox = 36;
    final int Velocidade = 6; // Velocidade que a boneca anda

    // Lógica da Animação
    int direcao = 0; // 0=Baixo, 1=Direita, 2=Esquerda, 3=Cima
    int frame = 0; // Qual perna está mexendo
    int contadorAnimacao = 0;
    boolean andando = false;

    // Imagens e Áudios
    Image imagemMapa;
    Image spriteSheet;
    Image luzMapa;
    Image imgNPC;
    Image imgEnfermeira;
    Image imgPorta0; // parte1.jpg (Vazia)
    Image imgPorta1; // parte2.jpg (1 Peça)
    Image imgPorta2; // parte3.jpg (2 Peças)
    Image imgPorta3; // parte4.jpg (Completa)
    Font fontePress;
    Clip somPassos;
    Clip ambiente;

    // Sistema de Pausa do Jogo (Tecla ESC)
    boolean pausado = false;
    int opcao = 0;

    // CONSTRUTOR: Roda uma única vez quando a tela do jogo é aberta
    public JogoExploracao() {
        // Carrega as imagens do HD para a memória
        imagemMapa = new ImageIcon("img/quarto.png").getImage();
        spriteSheet = new ImageIcon("img/personagem.png").getImage();
        luzMapa = new ImageIcon("img/luz-sombra-temp.png").getImage();
        imgNPC = new ImageIcon("img/npc.png").getImage();
        imgEnfermeira = new ImageIcon("img/enfermeira.png").getImage();
        imgPorta0 = new ImageIcon("img/parte1.png").getImage();
        imgPorta1 = new ImageIcon("img/parte2.png").getImage();
        imgPorta2 = new ImageIcon("img/parte3.png").getImage();
        imgPorta3 = new ImageIcon("img/parte4.png").getImage();

        try {
            // Tenta carregar a fonte igual fizemos no Menu
            fontePress = Font.createFont(Font.TRUETYPE_FONT, new File("fonte/press.ttf"));
            fontePress = fontePress.deriveFont(16f); // Tamanho 16 para caber na tela
        } catch (Exception e) {
            System.out.println("Erro ao carregar fonte no JogoExploracao");
            fontePress = new Font("Arial", Font.BOLD, 16); // Fallback caso dê erro
        }

        // Inicializa as nossas classes personalizadas
        sistemaColisao = new GerenciadorColisao();
        sistemaDebug = new GerenciadorDebug();

        int escalaJogo = 2; // Mapa é ampliado 2x

        // Coloca a Maria Clara na posição inicial (onde o jogo começa)
        mundoX = 75;
        mundoY = (320 * escalaJogo) - Altura_Hitbox - 20;

        // Tenta carregar os áudios e tocar a música de fundo em repetição
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("audio/passos.wav"));
            somPassos = AudioSystem.getClip();
            somPassos.open(audio);
        } catch (Exception e) { e.printStackTrace(); }

        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("audio/ambiente.wav"));
            ambiente = AudioSystem.getClip();
            ambiente.open(audio);
            FloatControl volume = (FloatControl) ambiente.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-15.0f); // Abaixa o volume para não estourar o ouvido
            ambiente.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) { e.printStackTrace(); }

        // Ativa a escuta das teclas do teclado para este painel
        addKeyListener(this);
        setFocusable(true);
    }

    // Permite que o GerenciadorDebug pegue as colisões para desenhar as linhas
    public GerenciadorColisao getSistemaColisao() {
        return sistemaColisao;
    }

    // O "Pincel" do Java: Roda o tempo todo, redesenhando a tela a 60 frames por segundo
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // LÓGICA DE CÂMERA: Mantém o boneco no meio da tela (centroX, centroY)
        // E move o mapa inteiro (cameraX, cameraY) no sentido contrário aos passos da Clara
        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;
        int cameraX = centroX - mundoX;
        int cameraY = centroY - mundoY;
        int escala = 2;

        // 1. Pinta a imagem do chão do mapa
        g2d.drawImage(imagemMapa, cameraX, cameraY, imagemMapa.getWidth(this)*escala, imagemMapa.getHeight(this)*escala, this);

        // ==========================================
        // DESENHA OS NPCs (SÓ NO MUNDO REAL)
        // ==========================================

        if (!mundoUmbra) {
            // Desenha o chão do hospital
            g2d.drawImage(imagemMapa, cameraX, cameraY, imagemMapa.getWidth(this)*escala, imagemMapa.getHeight(this)*escala, this);

            // POSIÇÃO DO PACIENTE:
            if (imgNPC != null) {
                int npcX = 1600; // Mude para mover o desenho para os lados
                int npcY = 300;  // Mude para mover o desenho para cima/baixo
                g2d.drawImage(imgNPC, cameraX + npcX, cameraY + npcY, 150, 170, this);
            }

            //  POSIÇÃO DA ENFERMEIRA:
            if (imgEnfermeira != null) {
                int enfermeiraX = 1230; // Mude para mover o desenho para os lados
                int enfermeiraY = 230;  // Mude para mover o desenho para cima/baixo
                g2d.drawImage(imgEnfermeira, cameraX + enfermeiraX, cameraY + enfermeiraY, 50, 70, this);
            }
        }
        // 2. Se a Clara tomou a pílula, pinta um filtro vermelho de "Pesadelo" por cima da tela
        if (mundoUmbra) {
            g2d.setColor(new Color(150, 0, 0, 70));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // 3. Se apertou a tecla 'H', chama a classe que desenha as caixas coloridas
        if (mostrarHitboxes) {
            sistemaDebug.desenharHitboxes(g2d, this, cameraX, cameraY);
        }

        // Faz o cálculo de qual quadradinho da imagem do boneco deve ser desenhado agora
        int sx1 = frame * TAMANHO;
        int sy1 = direcao * TAMANHO;
        int sx2 = sx1 + TAMANHO;
        int sy2 = sy1 + TAMANHO;

        // 4. LÓGICA DO ESPELHO (SÓ APARECE NO UMBRA)
        if (mundoUmbra) {
            int espelhoTelaX = cameraX + areaEspelho.x;
            int espelhoTelaY = cameraY + areaEspelho.y;

            // Se a Maria Clara pisar "na frente" do espelho invisível, desenha o reflexo dela!
            if (mundoX + 28 > areaEspelho.x && mundoX - 28 < areaEspelho.x + areaEspelho.width) {
                int basePersonagemMundoY = mundoY + 28;
                int baseEspelhoMundoY = areaEspelho.y + areaEspelho.height;

                if (basePersonagemMundoY >= baseEspelhoMundoY) {
                    int distanciaDoEspelho = basePersonagemMundoY - baseEspelhoMundoY;
                    int baseEspelhoTelaY = espelhoTelaY + areaEspelho.height;
                    int reflexoTelaY_Bottom = baseEspelhoTelaY - (distanciaDoEspelho/5);
                    int reflexoTelaY_Top = reflexoTelaY_Bottom - 56;

                    Shape clipOriginal = g2d.getClip();
                    g2d.setClip(espelhoTelaX, espelhoTelaY, areaEspelho.width, areaEspelho.height);

                    // AlphaComposite deixa o boneco do reflexo transparente (efeito de vidro/fantasma)
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

                    // Inverte o boneco (se eu olho pra direita, meu reflexo no espelho olha pra esquerda)
                    int direcaoReflexo = direcao;
                    if (direcao == 3) direcaoReflexo = 0;
                    else if (direcao == 0) direcaoReflexo = 3;
                    if (direcao == 1) direcaoReflexo = 2;
                    else if (direcao == 2) direcaoReflexo = 1;

                    int reflexoSy1 = direcaoReflexo * TAMANHO;
                    int reflexoSy2 = reflexoSy1 + TAMANHO;

                    // Desenha o boneco do reflexo
                    g2d.drawImage(spriteSheet, centroX + 28, reflexoTelaY_Top, centroX - 28, reflexoTelaY_Bottom, sx1, reflexoSy1, sx2, reflexoSy2, this);

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.setClip(clipOriginal);
                }
            }
        }

        // 5. Pinta a Maria Clara real no centro da tela
        g2d.drawImage(spriteSheet, centroX - 28, centroY - 28, centroX + 28, centroY + 28, sx1, sy1, sx2, sy2, this);
        // 6. Pinta a imagem preta de luz e sombra (efeito de escuridão) por cima de tudo
        g2d.drawImage(luzMapa, cameraX, cameraY, luzMapa.getWidth(this)*escala, luzMapa.getHeight(this)*escala, this);

        // ==========================================
        // TUTORIAL SUTIL NO CHÃO DO QUARTO
        // ==========================================
        if (opacidadeTutorial > 0.0f) {
            // Aplica a transparência ao pincel do Java
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadeTutorial));

            g2d.setColor(new Color(200, 200, 200)); // Cinza claro
            g2d.setFont(new Font("Serif", Font.ITALIC, 18)); // Fonte clássica de terror

            // Desenha os textos um pouco abaixo da personagem
            g2d.drawString("Use as setas ou W A S D para andar...", centroX - 140, centroY + 120);
            g2d.drawString("Pressione [E] para investigar...", centroX - 110, centroY + 150);

            // Restaura o pincel para 100% visível para não afetar o resto do jogo
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // ===============================================
        // TELAS DE POP-UP (Telas que abrem no meio do jogo)
        // ===============================================

        // ===============================================
        // CINEMÁTICA DA ÚLTIMA PEÇA (NO NPC)
        // ===============================================
        if (mostrandoImagemFinalNpc) {
            // Escurece o fundo
            g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int puzzleTamanho = 400;
            int px = centroX - (puzzleTamanho / 2);
            int py = centroY - (puzzleTamanho / 2);

            // Desenha a parte 4 (Cadeado completo)
            if (imgPorta3 != null) {
                g2d.drawImage(imgPorta3, px, py, puzzleTamanho, puzzleTamanho, this);
            }
            return; // O return bloqueia o resto da tela para focar só na imagem
        }

        // ===============================================
        // TEXTO FLUTUANTE DE ÁREA LIBERADA
        // ===============================================
        if (mostrarTextoAreaLiberada) {
            g2d.setColor(Color.GREEN);
            if (fontePress != null) {
                g2d.setFont(fontePress.deriveFont(20f)); // Aumenta a fonte para 20
            } else {
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
            }
            g2d.drawString("AREA LIBERADA NO MUNDO UMBRA", centroX - 250, centroY - 150);
        }

        // TELA DO ENIGMA DO ESPELHO (Imagem do relógio)
        if (mostrandoEspelho) {
            g2d.setColor(new Color(0, 0, 0, 220)); // Fundo preto transparente
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int imgTamanhoX = 500;
            int imgTamanhoY = 400;
            int px = centroX - (imgTamanhoX / 2);
            int py = centroY - (imgTamanhoY / 2);

            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(px, py, imgTamanhoX, imgTamanhoY);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(px, py, imgTamanhoX, imgTamanhoY);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("[IMAGEM DO RELÓGIO NO ESPELHO]", px + 50, py + 150);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("O relógio marca 04:10", px + 80, py + 200);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Pressione [ESC] ou [E] para fechar", centroX - 120, py + imgTamanhoY + 40);
            return; // O return faz com que ele pule tudo daqui para baixo (como os textos do boneco)
        }

        // TELA DO ENIGMA DA PORTA TRANCADA
        if (mostrandoPuzzlePorta) {g2d.setColor(new Color(0, 0, 0, 220));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int puzzleTamanho = 400;
            int px = centroX - (puzzleTamanho / 2);
            int py = centroY - (puzzleTamanho / 2);

            // Se pegou as 3 peças, desenha a tela final (parte 4)
            if (partesObjetoCircular == 3) {
                if (imgPorta3 != null) {
                    g2d.drawImage(imgPorta3, px, py, puzzleTamanho, puzzleTamanho, this);
                }

                // MENSAGEM COM A FONTE PRESS!
                g2d.setColor(Color.GREEN);
                g2d.setFont(fontePress);
                // Texto centralizado (ajustado para a fonte Press)
                g2d.drawString("AREA LIBERADA NO MUNDO UMBRA", centroX - 220, py + puzzleTamanho + 50);
            } else {
                // Desenha a parte correta dependendo de quantas peças ela tem
                if (partesObjetoCircular == 2 && imgPorta2 != null) {
                    g2d.drawImage(imgPorta2, px, py, puzzleTamanho, puzzleTamanho, this);
                } else if (partesObjetoCircular == 1 && imgPorta1 != null) {
                    g2d.drawImage(imgPorta1, px, py, puzzleTamanho, puzzleTamanho, this);
                } else if (imgPorta0 != null) {
                    g2d.drawImage(imgPorta0, px, py, puzzleTamanho, puzzleTamanho, this);
                }

                // Mostra quantas peças faltam
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString("Faltam peças (" + partesObjetoCircular + "/3)", centroX - 90, py + puzzleTamanho + 40);
            }

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("Pressione [ESC] ou [E] para fechar", centroX - 120, py + puzzleTamanho + 80);
            return;
        }

        // ===============================================
        // AVISOS NA TELA (Saber quando pode apertar 'E')
        // ===============================================
        Rectangle areaJogador = new Rectangle(mundoX, mundoY, Largura_Hitbox, Altura_Hitbox);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        if (!mundoUmbra) { // Mensagens do Mundo Real
            if (areaJogador.intersects(areaPilula)) g2d.drawString("Aperte [E] para tomar a Pilula", centroX - 120, centroY - 40);
            else if (areaJogador.intersects(areaNPC)) g2d.drawString("Aperte [E] para falar com o Paciente", centroX - 140, centroY - 40);
            else if (areaJogador.intersects(areaDocumento)) g2d.drawString("Aperte [E] para ler o Papel", centroX - 120, centroY - 40);
            else mensagemAviso = ""; // Se afastar, limpa a mensagem de diálogo
        } else { // Mensagens do Mundo Umbra
            if (areaJogador.intersects(areaCama)) g2d.drawString("Aperte [E] para Acordar", centroX - 120, centroY - 40);
            else if (areaJogador.intersects(areaPortaUmbra)) g2d.drawString("Aperte [E] para inspecionar a Porta", centroX - 130, centroY - 40);
            else if (areaJogador.intersects(areaEspelho)) g2d.drawString("Aperte [E] para olhar no Espelho", centroX - 130, centroY - 40);
            else if (areaJogador.intersects(areaGaveta)) g2d.drawString("Aperte [E] para abrir a Gaveta", centroX - 130, centroY - 40);
            else mensagemAviso = "";
        }

        // Desenha textos amarelados embaixo do jogador (Dicas, conversas)
        if (!mensagemAviso.isEmpty()) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(mensagemAviso, centroX - 180, centroY + 60);
        }

        // TELA DE PAUSA (ESC)
        if (pausado) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString(opcao == 0 ? "-> VOLTAR" : "   VOLTAR", centroX - 100, centroY);
            g2d.drawString(opcao == 1 ? "-> SAIR" : "   SAIR", centroX - 100, centroY + 60);
        }
    }

    // ===============================================
    // LÓGICA DO TECLADO (Quando o jogador aperta um botão)
    // ===============================================
    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        // Tecla H - Ativa o Desenho do Modo Debug (Ferramenta do Dev)
        // OBS: Usamos repaint() sempre que mudamos uma tela para que o Java pinte o frame imediatamente
        if (tecla == KeyEvent.VK_H) { mostrarHitboxes = !mostrarHitboxes; repaint(); return; }

        // Tecla ESC - Sai de puzzles ou abre o menu de pausa
        if (tecla == KeyEvent.VK_ESCAPE) {
            if (mostrandoPuzzlePorta) { mostrandoPuzzlePorta = false; repaint(); return; }
            if (mostrandoEspelho) { mostrandoEspelho = false; repaint(); return; }
            pausado = !pausado; repaint(); return;
        }

        // Lógica do Menu de Pausa (Sobe/Desce e Enter)
        if (pausado) {
            if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) opcao = 0;
            if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) opcao = 1;
            if (tecla == KeyEvent.VK_ENTER && opcao == 0) pausado = false;
            if (tecla == KeyEvent.VK_ENTER && opcao == 1) System.exit(0); // Fecha o jogo
            repaint(); return;
        }

        // Tecla E - Fecha os pop-ups se eles estiverem abertos
        if (mostrandoPuzzlePorta && tecla == KeyEvent.VK_E) { mostrandoPuzzlePorta = false; repaint(); return; }
        if (mostrandoEspelho && tecla == KeyEvent.VK_E) { mostrandoEspelho = false; repaint(); return; }

        // --- SISTEMA DE INTERAÇÃO (Tecla E) ---
        if (tecla == KeyEvent.VK_E) {
            Rectangle areaJogador = new Rectangle(mundoX, mundoY, Largura_Hitbox, Altura_Hitbox);

            // INTERAÇÕES NO MUNDO REAL (Hospital)
            if (!mundoUmbra) {
                // Entrar no Umbra
                if (areaJogador.intersects(areaPilula)) { mundoUmbra = true; repaint(); }

                // Conversar com o Paciente  (NPC)
                else if (areaJogador.intersects(areaNPC)) {
                    if (!pegouPecaNpc) {
                        if (sabePalavraMagica) {
                            pegouPecaNpc = true;
                            partesObjetoCircular++;

                            // 1. Inicia a Cinemática!
                            mostrandoImagemFinalNpc = true;
                            mensagemAviso = ""; // Limpa os avisos normais

                            // 2. Cria um relógio de 3 segundos (3000 milissegundos)
                            Timer timerAnimacao = new Timer(3000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent evt) {
                                    mostrandoImagemFinalNpc = false; // Esconde a imagem 4
                                    mostrarTextoAreaLiberada = true; // Mostra o texto verde
                                    repaint();

                                    // 3. Relógio de 4 segundos para o texto verde desaparecer e o jogo voltar ao normal
                                    Timer timerTexto = new Timer(4000, new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent evt2) {
                                            mostrarTextoAreaLiberada = false;
                                            repaint();
                                        }
                                    });
                                    timerTexto.setRepeats(false);
                                    timerTexto.start();
                                }
                            });
                            timerAnimacao.setRepeats(false); // Para o relógio rodar só uma vez
                            timerAnimacao.start();

                        } else {
                            mensagemAviso = "NPC: Eu tenho algo útil, mas... qual é a palavra mágica?";
                        }
                    } else {
                        mensagemAviso = "NPC: Vá em frente, você tem o que precisa.";
                    }
                    repaint();
                }

                // Tentar ler o Documento (Finaliza a Missão 1)
                else if (areaJogador.intersects(areaDocumento)) {
                    if (!portaUmbraDestrancada) { // Se não destrancou a porta, o papel é ilegível
                        mensagemAviso = "As letras estão borradas, parecem dançar. Não consigo ler..."; repaint();
                    } else { // Se destrancou, termina a missão
                        if (!leuDocumentoRecepcao) {
                            leuDocumentoRecepcao = true; documentosAchados++; missaoAtual = 2;

                            somPassos.stop(); andando = false; // Trava o boneco para não continuar andando de fundo

                            String txt = "CONTEÚDO DO PAPEL:\n'Relatório de Incidente - Casa de Repouso Elímar Gonzales...'\n\n[Missão 'Primeiros Passos' Concluída!]\n[Missão 2 Iniciada: O terror psicológico começa...]";
                            JOptionPane.showMessageDialog(this, txt, "Documento Encontrado", JOptionPane.INFORMATION_MESSAGE); // Caixa nativa do Windows
                            repaint();
                        } else { mensagemAviso = "Você já leu este documento."; repaint(); }
                    }
                }
            }
            // INTERAÇÕES NO MUNDO UMBRA (Pesadelo)
            else {
                // Acordar (Voltar ao Real)
                if (areaJogador.intersects(areaCama)) { mundoUmbra = false; repaint(); }

                // Abrir Puzzle da Porta
                else if (areaJogador.intersects(areaPortaUmbra) && !portaUmbraDestrancada) {
                    mostrandoPuzzlePorta = true;
                    if (partesObjetoCircular == 3) portaUmbraDestrancada = true;
                    repaint();
                }

                // Olhar o Espelho e pegar a Peça 1
                else if (areaJogador.intersects(areaEspelho)) {
                    mostrandoEspelho = true;
                    if (!pegouPecaEspelho) {
                        pegouPecaEspelho = true; partesObjetoCircular++;
                        mensagemAviso = "Você encontrou a 1ª parte no reflexo do espelho!";
                    }
                    repaint();
                }

                // Abrir a Gaveta, digitar a senha e pegar Peça 2 + Palavra
                else if (areaJogador.intersects(areaGaveta)) {
                    if (!pegouPecaGaveta) {
                        somPassos.stop(); andando = false;

                        String senha = JOptionPane.showInputDialog(this, "Cadeado de 4 dígitos (Digite a senha):");

                        // Senha correta validada
                        if (senha != null && senha.equals("0410")) {
                            pegouPecaGaveta = true; sabePalavraMagica = true; partesObjetoCircular++;
                            mensagemAviso = "Você achou a 2ª peça e um bilhete com a palavra 'Redenção'.";
                        } else if (senha != null && !senha.isEmpty()) {
                            mensagemAviso = "Senha incorreta. O cadeado não abriu.";
                        }
                        repaint();
                    }
                }
            }
        }

        // Se uma tela de Puzzle estiver aberta, o código para aqui para impedir a boneca de andar
        if (mostrandoPuzzlePorta || mostrandoEspelho || mostrandoImagemFinalNpc) return;

        // --- SISTEMA DE MOVIMENTAÇÃO (WASD ou Setinhas) ---
        boolean movendo = false;

        // Verifica com o GerenciadorColisao se a Clara pode dar o passo sem bater em nada
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) {
            int novoX = mundoX + Velocidade;
            if (sistemaColisao.verificarPosicaoValida(novoX, mundoY, Largura_Hitbox, Altura_Hitbox, mundoUmbra, portaUmbraDestrancada)) mundoX = novoX;
            direcao = 1; movendo = true;
        }
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) {
            int novoX = mundoX - Velocidade;
            if (sistemaColisao.verificarPosicaoValida(novoX, mundoY, Largura_Hitbox, Altura_Hitbox, mundoUmbra, portaUmbraDestrancada)) mundoX = novoX;
            direcao = 2; movendo = true;
        }
        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) {
            int novoY = mundoY - Velocidade;
            if (sistemaColisao.verificarPosicaoValida(mundoX, novoY, Largura_Hitbox, Altura_Hitbox, mundoUmbra, portaUmbraDestrancada)) mundoY = novoY;
            direcao = 3; movendo = true;
        }
        if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) {
            int novoY = mundoY + Velocidade;
            if (sistemaColisao.verificarPosicaoValida(mundoX, novoY, Largura_Hitbox, Altura_Hitbox, mundoUmbra, portaUmbraDestrancada)) mundoY = novoY;
            direcao = 0; movendo = true;
        }

        // --- CONTROLA O ÁUDIO E A ANIMAÇÃO ---
        if (movendo) {
            contadorAnimacao++;
            if (contadorAnimacao >= 4) { // Se deu X passos, troca o pé da animação
                contadorAnimacao = 0;
                frame++;
                if (frame > 3) frame = 0; // O sprite tem 4 colunas (0, 1, 2, 3)
            }
            if (!andando) { // Dá play no áudio se ele não estava tocando
                somPassos.setFramePosition(0);
                somPassos.loop(Clip.LOOP_CONTINUOUSLY);
                andando = true;
            }
            // Retira 5% da opacidade a cada "passo" do teclado
            if (opacidadeTutorial > 0.0f) {
                opacidadeTutorial -= 0.05f;
                // Impede que o número fique negativo
                if (opacidadeTutorial < 0.0f) {
                    opacidadeTutorial = 0.0f;
                }
            }
        }
        repaint(); // Desenha a tela novamente com a Clara na nova posição
    }

    // Quando o jogador solta o botão do teclado
    @Override public void keyReleased(KeyEvent e) {
        if (pausado || mostrandoPuzzlePorta || mostrandoEspelho) return;

        frame = 0; // Boneca volta para a pose parada com os pés juntos
        somPassos.stop(); // Para o áudio de passos
        andando = false;
        repaint();
    }

    // Método obrigatório do Java para o KeyListener, não usamos neste jogo
    @Override public void keyTyped(KeyEvent e) {}
}
