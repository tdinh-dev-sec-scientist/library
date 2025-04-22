package library.csci2010;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class guiFinal {

  private JFrame loginFrame;
  private JFrame mainFrame;
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JTabbedPane tabbedPane;
  private JTable booksTable;
  private DefaultTableModel booksTableModel;
  private JTextField searchField;
  private Library library;
  private Map<String, String> users = new HashMap<>();
  private Map<String, ImageIcon> bookImages = new HashMap<>();
  private DefaultTableModel loansModel;
  private JTable loansTable;
  private String[] selectedImagePath = { null };

  // Constructor initializes the application
  public guiFinal() {
    // Initialize library
    library = new Library();
    loadLibraryData();
    loadBookImages();
    // Add admin
    users.put("admin", "admin123");
    users.put("tdinh", "tdinh123");
    users.put("", "");

    // Initialize UI
    initializeLoginUI();
  }

  // Refresh all view
  private void refreshAllViews() {
    refreshBookTable();
    refreshGalleryTab();
    refreshLoanTable(loansModel);
    refreshUserTables(loansModel, loansModel);
  }

  // Log out
  private void logout() {
    try {
      library.saveBooksToFile("books.dat"); // Force save before exit
    } catch (IOException e) {
      System.out.println("Error saving book data: " + e.getMessage());
    }
    mainFrame.dispose();
    loginFrame.setVisible(true); // return to login screen
  }

  // Load Book Images
  private void loadBookImages() {
    File coversDir = new File("book_covers");
    if (coversDir.exists()) {
      for (Book book : library.getAllBooks()) {
        File imageFile = new File(coversDir, book.getIsbn() + ".png");
        if (imageFile.exists()) {
          ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
          Image img = icon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
          bookImages.put(book.getIsbn(), new ImageIcon(img));
        }
      }
    }
  }

  /*
   * Create Gallery Panel
   */
  private JPanel createGalleryPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // gallery panel with border
    JPanel mainGalleryPanel = new JPanel(new BorderLayout());
    mainGalleryPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1),
        "Gallery",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));

    // Panel contain book cards
    JPanel galleryGridPanel = new JPanel(new GridLayout(0, 5, 20, 20));
    galleryGridPanel.setBackground(Color.WHITE);

    for (Book book : library.getAllBooks()) {
      JPanel bookCard = createBookCard(book);
      galleryGridPanel.add(bookCard);
    }

    // Scroll pane for gallery
    JScrollPane scrollPane = new JScrollPane(galleryGridPanel);
    scrollPane.setBorder(null);

    // Set scroll pane properties
    mainGalleryPanel.add(scrollPane, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton refreshButton = new JButton("Refresh");
    refreshButton.setFont(new Font("Arial", Font.PLAIN, 15));
    refreshButton.addActionListener(e -> refreshAllViews());
    buttonPanel.add(refreshButton);

    // Add components to main panel
    panel.add(mainGalleryPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
  }

  // Validate login
  private void validateLogin() {
    String username = usernameField.getText();
    String password = new String(passwordField.getPassword());

    if (username.equals("Username"))
      username = "";
    if (password.equals("Password"))
      password = "";

    if (users.containsKey(username) && users.get(username).equals(password)) {
      loginFrame.dispose();
      loadLibraryData();
      createMainUI();
    } else {
      JOptionPane.showMessageDialog(loginFrame,
          "Invalid username or password.",
          "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
  }

  // Load existing library data from file
  private void loadLibraryData() {
    try {
      library.loadBooksFromFile("books.dat");
      System.out.println("Book data loaded successfully!");
    } catch (FileNotFoundException e) {
      System.out.println("No existing book data found. Starting with empty library.");
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Error loading book data: " + e.getMessage());
    }
  }

  // Create book card for gallery view
  private void initializeLoginUI() {
    loginFrame = new JFrame("Java Library Login");
    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    loginFrame.setSize(800, 600);
    loginFrame.setLocationRelativeTo(null);
    loginFrame.setResizable(true);
    createLoginUI();
  }

  private void createLoginUI() {
    // Main panel with background image
    JPanel backgroundPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Load background image
        try {
          Image backgroundImage = new ImageIcon(getClass().getResource("img.jpg")).getImage();
          g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } catch (Exception e) {
          // Fallback to gradient if image loading fails
          Graphics2D g2d = (Graphics2D) g.create();
          GradientPaint gradient = new GradientPaint(
              0, 0, new Color(102, 51, 153), // Dark purple
              getWidth(), getHeight(), new Color(138, 43, 226)); // Violet
          g2d.setPaint(gradient);
          g2d.fillRect(0, 0, getWidth(), getHeight());
          g2d.dispose();
          System.err.println("Error loading background image: " + e.getMessage());
        }
      }
    };
    backgroundPanel.setLayout(new BorderLayout());

    // Center Login Form
    JPanel centerPanel = new JPanel(new GridBagLayout());
    centerPanel.setOpaque(false);

    JPanel loginCard = createLoginCard();
    centerPanel.add(loginCard);

    backgroundPanel.add(centerPanel, BorderLayout.CENTER);

    // Footer Panel
    JPanel footerPanel = createFooterPanel();
    backgroundPanel.add(footerPanel, BorderLayout.SOUTH);

    loginFrame.setContentPane(backgroundPanel);
    loginFrame.setVisible(true);
  }

  // create Login card
  private JPanel createLoginCard() {
    JPanel loginCard = new JPanel(new GridBagLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        // Semi-transparent panel background
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g2d.setColor(new Color(100, 50, 150)); // Purple semi-transparent background
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        g2d.dispose();
      }
    };
    loginCard.setOpaque(false);
    loginCard.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    loginCard.setPreferredSize(new Dimension(400, 450)); // Set preferred size for the login card

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    // Login Title

    JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
    titleLabel.setFont(new Font("Felix Titling", Font.BOLD, 50));
    titleLabel.setForeground(Color.WHITE);
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weighty = 0.2;
    loginCard.add(titleLabel, gbc);

    // Username Field
    gbc.gridy = 1;
    gbc.weighty = 0;
    loginCard.add(createRoundedFieldPanel("Username", new ImageIcon(getClass().getResource("user8.png"))), gbc);

    // Password Field
    gbc.gridy = 2;
    loginCard.add(createRoundedPasswordFieldPanel("Password", new ImageIcon(getClass().getResource("key2.png"))),
        gbc);

    // Options Panel
    JPanel optionsPanel = new JPanel(new BorderLayout());
    optionsPanel.setOpaque(false);

    JCheckBox rememberMe = new JCheckBox("Remember me");
    rememberMe.setForeground(Color.WHITE);
    rememberMe.setOpaque(false);

    JLabel forgotPassword = new JLabel("Forgot password?");
    forgotPassword.setForeground(Color.WHITE);
    forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    forgotPassword.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JOptionPane.showMessageDialog(loginFrame, "Password reset feature coming soon!");
      }
    });

    optionsPanel.add(rememberMe, BorderLayout.WEST);
    optionsPanel.add(forgotPassword, BorderLayout.EAST);
    gbc.gridy = 3;
    loginCard.add(optionsPanel, gbc);

    // Login Button
    JButton loginButton = createLoginButton();
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.setOpaque(false);
    buttonPanel.add(loginButton);
    gbc.gridy = 4;
    loginCard.add(buttonPanel, gbc);

    // Registration Link
    JLabel registerLink = new JLabel("Don't have an account? Register", JLabel.CENTER);
    registerLink.setForeground(Color.WHITE);
    registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    registerLink.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        JOptionPane.showMessageDialog(loginFrame, "Registration feature coming soon!");
      }
    });
    JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    registerPanel.setOpaque(false);
    registerPanel.add(registerLink);
    gbc.gridy = 5;
    loginCard.add(registerPanel, gbc);

    return loginCard;
  }

  // Create login button
  private JButton createLoginButton() {
    JButton loginButton = new JButton("Login") {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        super.paintComponent(g);
      }
    };
    loginButton.setPreferredSize(new Dimension(310, 45));
    loginButton.setFont(new Font("Arial", Font.BOLD, 18));
    loginButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
    loginButton.setBackground(Color.WHITE);
    loginButton.setForeground(Color.BLACK);
    loginButton.setBorderPainted(false);
    loginButton.setFocusPainted(false);
    loginButton.setContentAreaFilled(false);
    loginButton.setOpaque(false);
    loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    loginButton.addActionListener(e -> validateLogin());
    return loginButton;
  }

  // Helper method to create rounded text field panels with icons
  private JPanel createRoundedFieldPanel(String placeholder, ImageIcon icon) {
    JPanel panel = new JPanel(new BorderLayout(10, 0)) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Semi-transparent white background
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        g2d.dispose();
      }
    };
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    panel.setPreferredSize(new Dimension(300, 50));

    // Scale icon
    Image scaledIcon = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
    JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
    iconLabel.setPreferredSize(new Dimension(24, 24));

    // Using the class field for usernameField
    usernameField = new JTextField();
    usernameField.setOpaque(false);
    usernameField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    usernameField.setForeground(Color.WHITE);
    usernameField.setCaretColor(Color.WHITE);
    usernameField.setText(placeholder);
    usernameField.setPreferredSize(new Dimension(200, 40));
    usernameField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Placeholder behavior
    usernameField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (usernameField.getText().equals(placeholder)) {
          usernameField.setText("");
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (usernameField.getText().isEmpty()) {
          usernameField.setText(placeholder);
        }
      }
    });

    panel.add(iconLabel, BorderLayout.WEST);
    panel.add(usernameField, BorderLayout.CENTER);

    return panel;
  }

  // Helper method to create rounded password field panels with icons
  private JPanel createRoundedPasswordFieldPanel(String placeholder, ImageIcon icon) {
    JPanel panel = new JPanel(new BorderLayout(10, 0)) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
        g2d.dispose();
      }
    };
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    panel.setPreferredSize(new Dimension(300, 50));

    // Scale icon
    Image scaledIcon = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
    JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
    iconLabel.setPreferredSize(new Dimension(24, 24));

    // Using the class field for passwordField
    passwordField = new JPasswordField();
    passwordField.setOpaque(false);
    passwordField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    passwordField.setForeground(Color.WHITE);
    passwordField.setCaretColor(Color.WHITE);
    passwordField.setEchoChar((char) 0);
    passwordField.setText(placeholder);
    passwordField.setPreferredSize(new Dimension(200, 40));
    passwordField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Password toggle icon
    JLabel togglePassword = new JLabel(new ImageIcon(getClass().getResource("eye-close.png")));
    togglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
    togglePassword.addMouseListener(new MouseAdapter() {
      boolean passwordVisible = false;

      @Override
      public void mouseClicked(MouseEvent e) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
          passwordField.setEchoChar((char) 0);
          togglePassword.setIcon(new ImageIcon(getClass().getResource("eye.png")));
        } else {
          if (!String.valueOf(passwordField.getPassword()).equals(placeholder)) {
            passwordField.setEchoChar('•');
          }
          togglePassword.setIcon(new ImageIcon(getClass().getResource("eye-close.png")));
        }
      }
    });

    // Add Enter key support for login
    passwordField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          validateLogin();
        }
      }
    });

    // Placeholder behavior
    passwordField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (String.valueOf(passwordField.getPassword()).equals(placeholder)) {
          passwordField.setText("");
          passwordField.setEchoChar('•');
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (passwordField.getPassword().length == 0) {
          passwordField.setEchoChar((char) 0);
          passwordField.setText(placeholder);
        }
      }
    });

    JPanel passwordWrapper = new JPanel(new BorderLayout());
    passwordWrapper.setOpaque(false);
    passwordWrapper.add(passwordField, BorderLayout.CENTER);
    passwordWrapper.add(togglePassword, BorderLayout.EAST);

    panel.add(iconLabel, BorderLayout.WEST);
    panel.add(passwordWrapper, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new BorderLayout());
    footerPanel.setOpaque(false);
    footerPanel.setPreferredSize(new Dimension(600, 50));

    JLabel footerLabel = new JLabel("© 2025 Truong's Library System | All Rights Reserved", JLabel.CENTER);
    footerLabel.setFont(new Font("Arial", Font.PLAIN, 15));
    footerLabel.setForeground(Color.WHITE);
    footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    footerPanel.add(footerLabel, BorderLayout.CENTER);

    return footerPanel;
  }

  /**
   * Create the main application UI
   */
  private void createMainUI() {
    mainFrame = new JFrame("Library Management System");
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setSize(1200, 800);
    mainFrame.setLocationRelativeTo(null);

    // Create header
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setPreferredSize(new Dimension(1200, 100));
    headerPanel.setOpaque(false);

    // Main panel with background image
    JPanel backgroundPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Load background image
        try {
          Image backgroundImage = new ImageIcon(getClass().getResource("newimg.jpg")).getImage();
          g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } catch (Exception e) {
          // Fallback to gradient if image loading fails
          Graphics2D g2d = (Graphics2D) g.create();
          GradientPaint gradient = new GradientPaint(
              0, 0, new Color(102, 51, 153), // Dark purple
              getWidth(), getHeight(), new Color(138, 43, 226)); // Violet
          g2d.setPaint(gradient);
          g2d.fillRect(0, 0, getWidth(), getHeight());
          g2d.dispose();
          System.err.println("Error loading background image: " + e.getMessage());
        }
      }
    };
    backgroundPanel.setLayout(new BorderLayout());

    // Search bar panel
    JPanel searchPanel = new JPanel(new BorderLayout());
    searchPanel.setOpaque(false);
    searchPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 30)); 
    searchPanel.setPreferredSize(new Dimension(300, 35));

    // Search field
    searchField = new JTextField() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty()) {
          Graphics2D g2 = (Graphics2D) g;
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(new Color(180, 180, 180));
          g2.setFont(getFont().deriveFont(16f));
          FontMetrics fm = g2.getFontMetrics();
          g2.drawString("Search...", 8, (getHeight() + fm.getAscent()) / 2 - 2);
        }
      }
    };
    // Create a combo box for search type (hidden but functional)
    JComboBox<String> searchTypeCombo = new JComboBox<>(new String[] { "Title", "Author", "ISBN" });
    searchTypeCombo.setVisible(false); // Hidden but still functional

    // Search document listener - performs search as user types
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        searchBooks();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        searchBooks();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        searchBooks();
      }
    });

    // Enter key listener
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          searchBooks();
        }
      }
    });

    searchField.setBorder(null);
    searchField.setOpaque(false);
    searchField.setMargin(new Insets(0, 20, 0, 20));

    // Search icon button
    JButton searchIconButton = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int diameter = 36; // Fixed size
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;
        g2.setColor(new Color(45, 45, 45));
        g2.fillOval(x, y, diameter, diameter);

        // Draw Icon
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.8f));
        int glassSize = diameter * 3 / 5;
        int glassX = x + (diameter - glassSize) / 2;
        int glassY = y + (diameter - glassSize) / 2;
        g2.drawOval(glassX, glassY, glassSize, glassSize);

        double angle = Math.PI / 4;
        int startX = glassX + (int) (glassSize * Math.cos(angle));
        int startY = glassY + (int) (glassSize * Math.sin(angle));
        g2.drawLine(startX, startY,
            startX + glassSize / 3,
            startY + glassSize / 3);
      }

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(35, 40);
      }
    };
    searchIconButton.setBorderPainted(false);
    searchIconButton.setFocusPainted(false);
    searchIconButton.setContentAreaFilled(false);
    searchIconButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    searchIconButton.addActionListener(e -> searchBooks());

    // Search bar panel
    JPanel searchBarPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = getHeight();
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
      }
    };
    searchBarPanel.setOpaque(false);
    searchBarPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0)); // Padding

    searchBarPanel.add(searchField, BorderLayout.CENTER);

    JPanel iconWrapper = new JPanel(new GridBagLayout());
    iconWrapper.setOpaque(false);
    iconWrapper.setPreferredSize(new Dimension(35, 20));
    iconWrapper.add(searchIconButton);

    searchBarPanel.add(iconWrapper, BorderLayout.EAST);
    searchPanel.add(searchBarPanel, BorderLayout.CENTER);

    headerPanel.add(searchPanel, BorderLayout.EAST);
    backgroundPanel.add(headerPanel, BorderLayout.NORTH);

    // Tabbed pane for different sections
    tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.setFont(new Font("Arial", Font.PLAIN, 15));

    // Custom UI
    tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
      @Override
      protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
          int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
          g2d.setColor(new Color(255, 255, 255, 150));
          g2d.fillRoundRect(x, y, w, h, 15, 15);
          g2d.setColor(Color.WHITE);
          g2d.drawRoundRect(x, y, w, h, 15, 15);
        }
        g2d.dispose();
      }

      @Override
      protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
          int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
          g2d.setColor(new Color(255, 255, 255, 100));
        } else {
          g2d.setColor(new Color(255, 255, 255, 50));
        }
        g2d.fillRoundRect(x, y, w, h, 15, 15);
        g2d.dispose();
      }

      @Override
      protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        // Do not paint content border
      }

      @Override
      protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
        return width + 20; // raise width
      }

      @Override
      protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 10; // raise height
      }

      @Override
      protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
          int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set text color
        g2.setColor(isSelected ? Color.BLACK : Color.WHITE);

        // Center text vertically
        int x = textRect.x;
        int y = textRect.y + metrics.getAscent();

        // Draw the title
        g2.drawString(title, x, y);
      }
    });

    // Set foreground color for all tabs (fallback)
    tabbedPane.setForeground(Color.WHITE);

    // Create and add tabs
    tabbedPane.addTab("Book Inventory", createInventoryPanel());
    tabbedPane.addTab("Add Book", createAddBookPanel());
    tabbedPane.addTab("Lending Management", createLendingPanel());
    tabbedPane.addTab("Book Gallery", createGalleryPanel());
    tabbedPane.addTab("User Management", createUserPanel());

    backgroundPanel.add(tabbedPane, BorderLayout.CENTER);

    // Create status bar
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    statusPanel.setOpaque(false);

    JLabel statusLabel = new JLabel("Ready | " + library.getAllBooks().size() + " books in system");
    statusLabel.setForeground(Color.WHITE);

    JButton logoutButton = new JButton("Log out") {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        super.paintComponent(g);
      }
    };
    logoutButton.setBackground(new Color(231, 76, 60));
    logoutButton.setForeground(Color.BLACK);
    logoutButton.setBorderPainted(false);
    logoutButton.setFocusPainted(false);
    logoutButton.setContentAreaFilled(false);
    logoutButton.setOpaque(false);
    logoutButton.addActionListener(e -> logout());

    statusPanel.add(statusLabel, BorderLayout.WEST);
    statusPanel.add(logoutButton, BorderLayout.EAST);

    backgroundPanel.add(statusPanel, BorderLayout.SOUTH);

    // Set content pane and display
    mainFrame.setContentPane(backgroundPanel);
    mainFrame.setVisible(true);
  }

  private void showIssueDetail(Book book) {
    JDialog dialog = new JDialog(mainFrame, "Issue Detail", true);
    dialog.setSize(400, 300);
    dialog.setLocationRelativeTo(mainFrame);

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Information panel
    JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    addInfoRow(infoPanel, "ISBN:", book.getIsbn());
    addInfoRow(infoPanel, "Title:", book.getTitle());
    addInfoRow(infoPanel, "Borrower:", book.getBorrower());
    addInfoRow(infoPanel, "Loan Date:", book.getLoanDate().toString());
    addInfoRow(infoPanel, "Due Date:", book.getDueDate().toString());
    addInfoRow(infoPanel, "Days Remaining:",
        String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), book.getDueDate())));

    panel.add(new JLabel("ISSUE DETAILS", JLabel.CENTER), BorderLayout.NORTH);
    panel.add(infoPanel, BorderLayout.CENTER);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(closeButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setContentPane(panel);
    dialog.setVisible(true);
  }

  private void showReturnDetail(Book book) {
    JDialog dialog = new JDialog(mainFrame, "Return Detail", true);
    dialog.setSize(400, 250);
    dialog.setLocationRelativeTo(mainFrame);

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // LATE DATE CALCULATOR
    long daysOverdue = ChronoUnit.DAYS.between(book.getDueDate(), LocalDate.now());
    boolean isOverdue = daysOverdue > 0;

    JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    addInfoRow(infoPanel, "ISBN:", book.getIsbn());
    addInfoRow(infoPanel, "Title:", book.getTitle());
    addInfoRow(infoPanel, "Borrower:", book.getBorrower());
    addInfoRow(infoPanel, "Loan Date:", book.getLoanDate().toString());
    addInfoRow(infoPanel, "Due Date:", book.getDueDate().toString());
    addInfoRow(infoPanel, "Status:", isOverdue ? "Overdue by " + daysOverdue + " days" : "Returned on time");

    JLabel detailPanel = new JLabel("RETURN DETAILS", JLabel.CENTER);
    detailPanel.setFont(new Font("Arial", Font.BOLD, 15));
    panel.add(detailPanel, BorderLayout.NORTH);
    panel.add(infoPanel, BorderLayout.CENTER);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(closeButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setContentPane(panel);
    dialog.setVisible(true);
  }

  private void addInfoRow(JPanel panel, String label, String value) {
    JLabel lbl = new JLabel(label);
    lbl.setFont(new Font("Arial", Font.BOLD, 12));
    panel.add(lbl);

    JLabel val = new JLabel(value != null ? value : "N/A");
    val.setFont(new Font("Arial", Font.PLAIN, 12));
    panel.add(val);
  }

  /**
   * Export the book list to a file
   */
  private void exportBookList() {
    try (PrintWriter writer = new PrintWriter(new File("book_list.csv"))) {
      StringBuilder sb = new StringBuilder();
      sb.append("ISBN,Title,Author,Year,Type,Details,Status\n");

      for (Book book : library.getAllBooks()) {
        sb.append(book.getIsbn()).append(",");
        sb.append(book.getTitle()).append(",");
        sb.append(book.getAuthor()).append(",");
        sb.append(book.getPublicationYear()).append(",");
        sb.append(book instanceof FictionBook ? "Fiction"
            : book instanceof NonFictionBook ? "Non-Fiction" : "Reference").append(",");
        sb.append(book instanceof FictionBook ? ((FictionBook) book).getGenre()
            : book instanceof NonFictionBook ? ((NonFictionBook) book).getSubject()
                : ((ReferenceBook) book).getCategory())
            .append(",");
        sb.append(book.isOnLoan() ? "On Loan" : "Available").append("\n");
      }

      writer.write(sb.toString());
      JOptionPane.showMessageDialog(mainFrame, "Book list exported successfully!", "Export Success",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog(mainFrame, "Error exporting book list: " + e.getMessage(), "Export Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Create panel for book inventory display
   */
  private JPanel createInventoryPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel inventoryPanel = new JPanel(new BorderLayout());
    inventoryPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1),
        "Book Inventory",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));

    // Create table model with columns
    booksTableModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Make table read-only
      }
    };

    booksTableModel.addColumn("ISBN");
    booksTableModel.addColumn("Title");
    booksTableModel.addColumn("Author");
    booksTableModel.addColumn("Year");
    booksTableModel.addColumn("Type");
    booksTableModel.addColumn("Details");
    booksTableModel.addColumn("Status");

    // Create table
    booksTable = new JTable(booksTableModel);
    booksTable.setRowHeight(30);
    booksTable.setFont(new Font("Arial", Font.PLAIN, 15));
    booksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
    booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    booksTable.setAutoCreateRowSorter(true);

    // Set column widths
    booksTable.getColumnModel().getColumn(0).setPreferredWidth(100); // ISBN
    booksTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Title
    booksTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Author
    booksTable.getColumnModel().getColumn(3).setPreferredWidth(80); // Year
    booksTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Type
    booksTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Details
    booksTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Status

    // Add right-click context menu
    JPopupMenu contextMenu = new JPopupMenu();
    JMenuItem viewItem = new JMenuItem("View Details");
    JMenuItem editItem = new JMenuItem("Edit Book");
    JMenuItem removeItem = new JMenuItem("Remove Book");

    viewItem.addActionListener(e -> viewSelectedBook());
    editItem.addActionListener(e -> editSelectedBook());
    removeItem.addActionListener(e -> removeSelectedBook());

    contextMenu.add(viewItem);
    contextMenu.add(editItem);
    contextMenu.add(removeItem);

    booksTable.setComponentPopupMenu(contextMenu);

    // Add to scrollpane
    JScrollPane scrollPane = new JScrollPane(booksTable);
    inventoryPanel.add(scrollPane, BorderLayout.CENTER);

    // Add control buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JButton refreshButton = new JButton("Refresh");
    refreshButton.setFont(new Font("Arial", Font.PLAIN, 15));
    refreshButton.addActionListener(e -> refreshBookTable());

    JButton exportButton = new JButton("Export List");
    exportButton.setFont(new Font("Arial", Font.PLAIN, 15));
    exportButton.addActionListener(e -> exportBookList());

    buttonPanel.add(refreshButton);
    buttonPanel.add(exportButton);
    panel.add(inventoryPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    // Initial table load
    refreshBookTable();

    return panel;
  }

  // Remove Book
  private void removeSelectedBook() {
    int selectedRow = booksTable.getSelectedRow();
    if (selectedRow >= 0) {
      int confirm = JOptionPane.showConfirmDialog(mainFrame,
          "Are you sure you want to delete this book?",
          "Confirm Delete", JOptionPane.YES_NO_OPTION);

      if (confirm == JOptionPane.YES_OPTION) {
        String isbn = (String) booksTable.getValueAt(selectedRow, 0);
        List<Book> books = library.searchBooksByISBN(isbn);
        Book book = books.isEmpty() ? null : books.get(0);

        if (book != null) {
          try {
            try {
              library.removeBook(book.getIsbn());
              library.saveBooksToFile("books.dat");
              refreshBookTable();
              JOptionPane.showMessageDialog(mainFrame,
                  "Book deleted successfully!",
                  "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (BookNotFoundException ex) {
              JOptionPane.showMessageDialog(mainFrame,
                  "Error deleting book: " + ex.getMessage(),
                  "Error", JOptionPane.ERROR_MESSAGE);
            }
          } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainFrame,
                "Error deleting book: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    } else {
      JOptionPane.showMessageDialog(mainFrame,
          "Please select a book to delete",
          "No Selection", JOptionPane.WARNING_MESSAGE);
    }
  }

  // Edit Book
  private void editSelectedBook() {
    int selectedRow = booksTable.getSelectedRow();
    if (selectedRow >= 0) {
      String isbn = (String) booksTable.getValueAt(selectedRow, 0);
      List<Book> books = library.searchBooksByISBN(isbn);
      Book book = books.isEmpty() ? null : books.get(0);

      if (book != null) {
        // Create edit dialog
        JDialog editDialog = createEditBookDialog(book);
        editDialog.setVisible(true);
      }
    } else {
      JOptionPane.showMessageDialog(mainFrame,
          "Please select a book to edit",
          "No Selection", JOptionPane.WARNING_MESSAGE);
    }
  }

  // View Book's Information
  private void viewSelectedBook() {
    int selectedRow = booksTable.getSelectedRow();
    if (selectedRow >= 0) {
      String isbn = (String) booksTable.getValueAt(selectedRow, 0);
      List<Book> books = library.searchBooksByISBN(isbn);
      Book book = books.isEmpty() ? null : books.get(0);

      if (book != null) {
        showBookDetailsDialog(book);
      }
    } else {
      JOptionPane.showMessageDialog(mainFrame,
          "Please select a book to view details",
          "No Selection", JOptionPane.WARNING_MESSAGE);
    }
  }

  /*
   * Create panel for adding books
   */
  private JPanel createAddBookPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Split panel into form and preview sections
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(0.65);
    splitPane.setDividerLocation(700);
    splitPane.setBorder(BorderFactory.createEmptyBorder());

    // Form panel
    JPanel formPanel = new JPanel(new BorderLayout());
    formPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "Book Information",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 16),
            Color.BLACK),
        BorderFactory.createEmptyBorder(20, 20, 20, 20)));

    // Create form fields
    JPanel fieldsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Book type selection
    JLabel typeLabel = new JLabel("Book Type:");
    typeLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JComboBox<String> typeCombo = new JComboBox<>(new String[] {
        "Fiction Book", "Non-Fiction Book", "Reference Book"
    });
    typeCombo.setFont(new Font("Arial", Font.PLAIN, 15));

    // ISBN
    JLabel isbnLabel = new JLabel("ISBN:");
    isbnLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField isbnField = new JTextField(20);
    isbnField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Title
    JLabel titleLabel = new JLabel("Title:");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField titleField = new JTextField(20);
    titleField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Author
    JLabel authorLabel = new JLabel("Author:");
    authorLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField authorField = new JTextField(20);
    authorField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Year
    JLabel yearLabel = new JLabel("Publication Year:");
    yearLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField yearField = new JTextField(20);
    yearField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Type-specific field (changes based on book type)
    JLabel specificLabel = new JLabel("Genre:");
    specificLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField specificField = new JTextField(20);
    specificField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Update specific field label based on book type selection
    typeCombo.addActionListener(e -> {
      String selectedType = (String) typeCombo.getSelectedItem();
      if ("Fiction Book".equals(selectedType)) {
        specificLabel.setText("Genre:");
      } else if ("Non-Fiction Book".equals(selectedType)) {
        specificLabel.setText("Subject:");
      } else if ("Reference Book".equals(selectedType)) {
        specificLabel.setText("Category:");
      }
    });

    // Add fields to the panel
    gbc.gridx = 0;
    gbc.gridy = 0;
    fieldsPanel.add(typeLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(typeCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.0;
    fieldsPanel.add(isbnLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(isbnField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.0;
    fieldsPanel.add(titleLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(titleField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 0.0;
    fieldsPanel.add(authorLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(authorField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.weightx = 0.0;
    fieldsPanel.add(yearLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(yearField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.weightx = 0.0;
    fieldsPanel.add(specificLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(specificField, gbc);

    // Book cover image selection
    JLabel imageLabel = new JLabel("Book Cover:");
    imageLabel.setFont(new Font("Arial", Font.BOLD, 15));

    JPanel imagePanel = new JPanel(new BorderLayout());
    JLabel imagePreview = new JLabel();
    imagePreview.setPreferredSize(new Dimension(150, 200));
    imagePreview.setHorizontalAlignment(JLabel.CENTER);
    imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    imagePreview.setBackground(Color.WHITE);
    imagePreview.setOpaque(true);

    JButton browseButton = new JButton("Browse Image");
    browseButton.setFont(new Font("Arial", Font.PLAIN, 15));

    final String[] selectedImagePath = { null };

    browseButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        @Override
        public boolean accept(File f) {
          if (f.isDirectory())
            return true;
          String name = f.getName().toLowerCase();
          return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
              name.endsWith(".png") || name.endsWith(".gif");
        }

        @Override
        public String getDescription() {
          return "Image Files (*.jpg, *.jpeg, *.png, *.gif)";
        }
      });

      int result = fileChooser.showOpenDialog(mainFrame);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        selectedImagePath[0] = selectedFile.getAbsolutePath();
        ImageIcon icon = new ImageIcon(selectedImagePath[0]);
        Image img = icon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
        imagePreview.setIcon(new ImageIcon(img));
      }
    });

    imagePanel.add(imagePreview, BorderLayout.CENTER);
    imagePanel.add(browseButton, BorderLayout.SOUTH);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.weightx = 0.0;
    fieldsPanel.add(imageLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    fieldsPanel.add(imagePanel, gbc);

    // Add buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton clearButton = new JButton("Clear");
    clearButton.setFont(new Font("Arial", Font.PLAIN, 15));
    clearButton.addActionListener(e -> {
      isbnField.setText("");
      titleField.setText("");
      authorField.setText("");
      yearField.setText("");
      specificField.setText("");
      imagePreview.setIcon(null);
      selectedImagePath[0] = null;
    });

    JButton addButton = new JButton("Add Book");
    addButton.setFont(new Font("Arial", Font.PLAIN, 15));
    addButton.setBackground(Color.BLACK);
    addButton.setForeground(Color.BLACK);

    addButton.addActionListener(e -> {
      try {
        // Validate input
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearText = yearField.getText().trim();
        String specificValue = specificField.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() ||
            yearText.isEmpty() || specificValue.isEmpty()) {
          JOptionPane.showMessageDialog(mainFrame,
              "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        int year;
        try {
          year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
          JOptionPane.showMessageDialog(mainFrame,
              "Year must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        // Create book based on type
        Book book;
        String selectedType = (String) typeCombo.getSelectedItem();

        switch (selectedType) {
          case "Fiction Book":
            book = new FictionBook(isbn, title, author, year, specificValue);
            break;
          case "Non-Fiction Book":
            book = new NonFictionBook(isbn, title, author, year, specificValue);
            break;
          case "Reference Book":
            book = new ReferenceBook(isbn, title, author, year, specificValue);
            break;
          default:
            throw new IllegalArgumentException("Invalid book type");
        }

        // Save image if selected
        if (selectedImagePath[0] != null) {
          saveBookImage(isbn, selectedImagePath[0]); // Save Image
          ImageIcon icon = new ImageIcon(selectedImagePath[0]);
          Image img = icon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
          bookImages.put(isbn, new ImageIcon(img));
        }
        refreshGalleryTab();

        // Add book to library
        library.addBook(book);

        // Save data
        library.saveBooksToFile("books.dat");

        // Show success message
        JOptionPane.showMessageDialog(mainFrame,
            "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        clearButton.doClick();

        // Refresh
        refreshAllViews();

      } catch (Exception ex) {
        JOptionPane.showMessageDialog(mainFrame,
            "Error adding book: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    buttonPanel.add(clearButton);
    buttonPanel.add(addButton);

    formPanel.add(fieldsPanel, BorderLayout.CENTER);
    formPanel.add(buttonPanel, BorderLayout.SOUTH);

    // Preview panel
    JPanel previewPanel = new JPanel(new BorderLayout());
    previewPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            "Preview",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 16),
            Color.BLACK),
        BorderFactory.createEmptyBorder(20, 20, 20, 20)));

    JPanel previewContentPanel = new JPanel();
    previewContentPanel.setLayout(new BoxLayout(previewContentPanel, BoxLayout.Y_AXIS));
    previewContentPanel.setBackground(Color.WHITE);
    previewContentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    JLabel previewTitle = new JLabel("Book Preview");
    previewTitle.setFont(new Font("Arial", Font.BOLD, 18));
    previewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel previewDesc = new JLabel("Enter book details to see preview");
    previewDesc.setFont(new Font("Arial", Font.ITALIC, 15));
    previewDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel previewCover = new JLabel();
    previewCover.setAlignmentX(Component.CENTER_ALIGNMENT);
    previewCover.setPreferredSize(new Dimension(180, 240));
    previewCover.setMaximumSize(new Dimension(180, 240));

    JTextArea previewDetails = new JTextArea(8, 20);
    previewDetails.setEditable(false);
    previewDetails.setFont(new Font("Arial", Font.PLAIN, 15));
    previewDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
    previewDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    previewDetails.setLineWrap(true);
    previewDetails.setWrapStyleWord(true);

    previewContentPanel.add(Box.createVerticalStrut(20));
    previewContentPanel.add(previewTitle);
    previewContentPanel.add(Box.createVerticalStrut(10));
    previewContentPanel.add(previewDesc);
    previewContentPanel.add(Box.createVerticalStrut(20));
    previewContentPanel.add(previewCover);
    previewContentPanel.add(Box.createVerticalStrut(20));
    previewContentPanel.add(previewDetails);

    // Update preview as user types
    DocumentListener previewUpdater = new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        updatePreview();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updatePreview();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updatePreview();
      }

      private void updatePreview() {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String year = yearField.getText().trim();
        String specific = specificField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();

        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(title.isEmpty() ? "[Title]" : title).append("\n");
        sb.append("Author: ").append(author.isEmpty() ? "[Author]" : author).append("\n");
        sb.append("ISBN: ").append(isbn.isEmpty() ? "[ISBN]" : isbn).append("\n");
        sb.append("Publication Year: ").append(year.isEmpty() ? "[Year]" : year).append("\n");

        if ("Fiction Book".equals(type)) {
          sb.append("Type: Fiction\n");
          sb.append("Genre: ").append(specific.isEmpty() ? "[Genre]" : specific);
        } else if ("Non-Fiction Book".equals(type)) {
          sb.append("Type: Non-Fiction\n");
          sb.append("Subject: ").append(specific.isEmpty() ? "[Subject]" : specific);
        } else if ("Reference Book".equals(type)) {
          sb.append("Type: Reference\n");
          sb.append("Category: ").append(specific.isEmpty() ? "[Category]" : specific);
        }

        previewDetails.setText(sb.toString());

        if (selectedImagePath[0] != null) {
          ImageIcon icon = new ImageIcon(selectedImagePath[0]);
          Image img = icon.getImage().getScaledInstance(180, 240, Image.SCALE_SMOOTH);
          previewCover.setIcon(new ImageIcon(img));
        } else {
          previewCover.setIcon(null);
        }

        previewTitle.setText(title.isEmpty() ? "Book Preview" : title);
        previewDesc.setText(author.isEmpty() ? "Enter book details to see preview" : "by " + author);
      }

    };
    // Add listeners to update preview
    isbnField.getDocument().addDocumentListener(previewUpdater);
    titleField.getDocument().addDocumentListener(previewUpdater);
    authorField.getDocument().addDocumentListener(previewUpdater);
    yearField.getDocument().addDocumentListener(previewUpdater);
    specificField.getDocument().addDocumentListener(previewUpdater);

    previewPanel.add(new JScrollPane(previewContentPanel), BorderLayout.CENTER);

    // Add panels to split pane
    splitPane.setLeftComponent(formPanel);
    splitPane.setRightComponent(previewPanel);

    panel.add(splitPane, BorderLayout.CENTER);

    return panel;
  }

  /**
   * Create panel for user management
   */
  private JPanel createUserPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Create split pane for the two main sections
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setDividerLocation(350);
    splitPane.setBorder(BorderFactory.createEmptyBorder());

    // 1. Active Loans Panel (Top)
    JPanel activeLoansPanel = new JPanel(new BorderLayout());
    activeLoansPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1), // Violet border
        "Active Loans",
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));

    // Active loans table model
    DefaultTableModel activeLoansModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Make table read-only
      }
    };
    activeLoansModel.addColumn("Book Title");
    activeLoansModel.addColumn("Borrower");
    activeLoansModel.addColumn("Loan Date");
    activeLoansModel.addColumn("Due Date");
    activeLoansModel.addColumn("Days Remaining");

    JTable activeLoansTable = new JTable(activeLoansModel);
    activeLoansTable.setRowHeight(25);
    activeLoansTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
    activeLoansTable.setFont(new Font("Arial", Font.PLAIN, 15));

    // Populate active loans
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    for (Book book : library.getAllBooks()) {
      if (book.isOnLoan()) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), book.getDueDate());
        String status = daysRemaining > 0 ? String.valueOf(daysRemaining) : "Overdue (" + (-daysRemaining) + " days)";

        activeLoansModel.addRow(new Object[] {
            book.getTitle(),
            book.getBorrower(),
            book.getLoanDate().format(formatter),
            book.getDueDate().format(formatter),
            status
        });
      }
    }

    // Add table to scroll pane
    JScrollPane activeLoansScroll = new JScrollPane(activeLoansTable);
    activeLoansPanel.add(activeLoansScroll, BorderLayout.CENTER);

    // 2. Return History Panel (Bottom)
    JPanel returnHistoryPanel = new JPanel(new BorderLayout());
    returnHistoryPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1), // Violet border
        "Return History",
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));

    // Return history table model
    DefaultTableModel returnHistoryModel = new DefaultTableModel() {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false; // Make table read-only
      }
    };
    returnHistoryModel.addColumn("Book Title");
    returnHistoryModel.addColumn("Borrower");
    returnHistoryModel.addColumn("Loan Date");
    returnHistoryModel.addColumn("Return Date");
    returnHistoryModel.addColumn("Status");

    JTable returnHistoryTable = new JTable(returnHistoryModel);
    returnHistoryTable.setRowHeight(25);
    returnHistoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
    returnHistoryTable.setFont(new Font("Arial", Font.PLAIN, 15));

    // Example + not do yet
    returnHistoryModel.addRow(new Object[] {
        "Introduction to Java",
        "John Doe",
        "01/15/2023",
        "01/30/2023",
        "Returned on time"
    });
    returnHistoryModel.addRow(new Object[] {
        "Advanced Algorithms",
        "Jane Smith",
        "02/01/2023",
        "02/20/2023",
        "Overdue (5 days)"
    });

    // Add table to scroll pane
    JScrollPane returnHistoryScroll = new JScrollPane(returnHistoryTable);
    returnHistoryPanel.add(returnHistoryScroll, BorderLayout.CENTER);

    // Add both panels to split pane
    splitPane.setTopComponent(activeLoansPanel);
    splitPane.setBottomComponent(returnHistoryPanel);

    // Add controls panel at the bottom
    JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    JButton refreshButton = new JButton("Refresh");
    refreshButton.setFont(new Font("Arial", Font.PLAIN, 15));
    refreshButton.addActionListener(e -> {
      // Refresh both tables
      refreshUserTables(activeLoansModel, returnHistoryModel);
    });

    JButton exportButton = new JButton("Export Data");
    exportButton.setFont(new Font("Arial", Font.PLAIN, 15));
    exportButton.addActionListener(e -> exportUserData(activeLoansModel, returnHistoryModel));

    controlsPanel.add(refreshButton);
    controlsPanel.add(Box.createHorizontalStrut(10));
    controlsPanel.add(exportButton);

    // Add components to main panel
    panel.add(splitPane, BorderLayout.CENTER);
    panel.add(controlsPanel, BorderLayout.SOUTH);

    return panel;
  }

  // Helper method to refresh user tables
  private void refreshUserTables(DefaultTableModel activeLoansModel, DefaultTableModel returnHistoryModel) {
    // Clear existing data
    activeLoansModel.setRowCount(0);
    returnHistoryModel.setRowCount(0);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Active loans
    for (Book book : library.getAllBooks()) {
      if (book.isOnLoan()) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), book.getDueDate());
        String status = daysRemaining > 0 ? daysRemaining + " days remaining"
            : "Overdue (" + (-daysRemaining) + " days)";

        activeLoansModel.addRow(new Object[] {
            book.getTitle(),
            book.getBorrower(),
            book.getLoanDate().format(formatter),
            book.getDueDate().format(formatter),
            status
        });
      }
    }
  }

  // Export user data to CSV
  private void exportUserData(DefaultTableModel activeLoansModel, DefaultTableModel returnHistoryModel) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export User Data");
    fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

    if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (!file.getName().toLowerCase().endsWith(".csv")) {
        file = new File(file.getPath() + ".csv");
      }

      try (PrintWriter writer = new PrintWriter(file)) {
        // Write active loans
        writer.println("ACTIVE LOANS");
        for (int i = 0; i < activeLoansModel.getColumnCount(); i++) {
          writer.print(activeLoansModel.getColumnName(i));
          if (i < activeLoansModel.getColumnCount() - 1) {
            writer.print(",");
          }
        }
        writer.println();

        for (int row = 0; row < activeLoansModel.getRowCount(); row++) {
          for (int col = 0; col < activeLoansModel.getColumnCount(); col++) {
            writer.print(activeLoansModel.getValueAt(row, col));
            if (col < activeLoansModel.getColumnCount() - 1) {
              writer.print(",");
            }
          }
          writer.println();
        }

        // Write return history
        writer.println("\nRETURN HISTORY");
        for (int i = 0; i < returnHistoryModel.getColumnCount(); i++) {
          writer.print(returnHistoryModel.getColumnName(i));
          if (i < returnHistoryModel.getColumnCount() - 1) {
            writer.print(",");
          }
        }
        writer.println();

        for (int row = 0; row < returnHistoryModel.getRowCount(); row++) {
          for (int col = 0; col < returnHistoryModel.getColumnCount(); col++) {
            writer.print(returnHistoryModel.getValueAt(row, col));
            if (col < returnHistoryModel.getColumnCount() - 1) {
              writer.print(",");
            }
          }
          writer.println();
        }

        JOptionPane.showMessageDialog(mainFrame,
            "User data exported successfully!",
            "Export Complete",
            JOptionPane.INFORMATION_MESSAGE);
      } catch (FileNotFoundException e) {
        JOptionPane.showMessageDialog(mainFrame,
            "Error exporting data: " + e.getMessage(),
            "Export Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * Create panel for lending management
   */
  private JPanel createLendingPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Split into two sections: lending form and lending history
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    splitPane.setDividerLocation(350);

    // Lending form panel
    JPanel lendingFormPanel = new JPanel(new BorderLayout());
    lendingFormPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1),
        "Book Lending",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));

    // Form fields using grid layout
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Book selection
    JLabel bookLabel = new JLabel("Book:");
    bookLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JComboBox<String> bookCombo = new JComboBox<>();
    bookCombo.setFont(new Font("Arial", Font.PLAIN, 15));

    // Fill book combo with available books
    for (Book book : library.getAllBooks()) {
      if (!book.isOnLoan()) {
        bookCombo.addItem(book.getTitle() + " (" + book.getIsbn() + ")");
      }
    }

    // Borrower name
    JLabel borrowerLabel = new JLabel("Borrower Name:");
    borrowerLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField borrowerField = new JTextField(20);
    borrowerField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Borrower ID
    JLabel idLabel = new JLabel("Borrower ID:");
    idLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField idField = new JTextField(20);
    idField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Loan date selection
    JLabel loanDateLabel = new JLabel("Loan Date & Due Date: ");
    loanDateLabel.setFont(new Font("Arial", Font.BOLD, 15));

    // Due date selection
    JLabel dueDateLabel = new JLabel("");
    dueDateLabel.setFont(new Font("Arial", Font.BOLD, 15));

    // Date pickers for loan and due dates
    JPanel datePanel = new JPanel(new GridLayout(2, 2, 5, 5));
    datePanel.setFont(new Font("Arial", Font.PLAIN, 15));

    // Loan Date Spinner
    SpinnerDateModel loanModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
    JSpinner loanSpinner = new JSpinner(loanModel);
    JSpinner.DateEditor loanEditor = new JSpinner.DateEditor(loanSpinner, "MM/dd/yyyy");
    loanSpinner.setEditor(loanEditor);

    // Due Date Spinner (default +7 days)
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 7);
    SpinnerDateModel dueModel = new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH);
    JSpinner dueSpinner = new JSpinner(dueModel);
    JSpinner.DateEditor dueEditor = new JSpinner.DateEditor(dueSpinner, "MM/dd/yyyy");
    dueSpinner.setEditor(dueEditor);

    datePanel.add(loanDateLabel);
    datePanel.add(loanSpinner);
    datePanel.add(dueDateLabel);
    datePanel.add(dueSpinner);

    // Add components to the form
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    formPanel.add(bookLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    formPanel.add(bookCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.0;
    formPanel.add(borrowerLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    formPanel.add(borrowerField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.0;
    formPanel.add(idLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    formPanel.add(idField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 0.0;
    formPanel.add(loanDateLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    formPanel.add(datePanel, gbc);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton lendButton = new JButton("Lend Book");
    lendButton.setFont(new Font("Arial", Font.PLAIN, 15));
    lendButton.setBackground(Color.BLACK);
    lendButton.setForeground(Color.BLACK);

    lendButton.addActionListener(e -> {
      if (bookCombo.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(mainFrame,
            "No book selected or no books available for lending.",
            "Lending Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String borrower = borrowerField.getText().trim();
      String id = idField.getText().trim();

      if (borrower.isEmpty() || id.isEmpty()) {
        JOptionPane.showMessageDialog(mainFrame,
            "Borrower name and ID are required.",
            "Lending Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Extract ISBN from selection
      String selection = (String) bookCombo.getSelectedItem();
      String isbn = selection.substring(selection.lastIndexOf("(") + 1, selection.lastIndexOf(")"));

      List<Book> books = library.searchBooksByISBN(isbn);
      Book book = books.isEmpty() ? null : books.get(0);
      if (book != null && !book.isOnLoan()) {
        // Set loan status
        book.setOnLoan(true);
        book.setBorrower(borrower);

        // Get loan and due dates from spinners
        Date loanDate = (Date) loanSpinner.getValue();
        Date dueDate = (Date) dueSpinner.getValue();

        // Convert to LocalDate
        LocalDate loanLocalDate = loanDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dueLocalDate = dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Set dates in book
        book.setLoanDate(loanLocalDate);
        book.setDueDate(dueLocalDate);

        // Add to lending history
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        loansModel.addRow(new Object[] {
            book.getIsbn(),
            book.getTitle(),
            borrower,
            loanLocalDate.format(formatter),
            dueLocalDate.format(formatter),
            "On Loan"
        });
        // Save library state
        try {
          library.saveBooksToFile("books.dat");

          JOptionPane.showMessageDialog(mainFrame,
              "Book loaned successfully to " + borrower + ".",
              "Lending Success", JOptionPane.INFORMATION_MESSAGE);

          // Clear fields
          borrowerField.setText("");
          idField.setText("");

          // Refresh all views
          refreshAllViews();

          // Refresh book combo
          bookCombo.removeItem(selection);

        } catch (IOException ex) {
          JOptionPane.showMessageDialog(mainFrame,
              "Error saving lending record: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        JOptionPane.showMessageDialog(mainFrame,
            "Book is not available for lending.",
            "Lending Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    buttonPanel.add(lendButton);

    lendingFormPanel.add(formPanel, BorderLayout.CENTER);
    lendingFormPanel.add(buttonPanel, BorderLayout.SOUTH);

    // Lending history panel
    JPanel historyPanel = new JPanel(new BorderLayout());
    historyPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Color.BLACK, 1),
        "Current Loans",
        TitledBorder.DEFAULT_JUSTIFICATION,
        TitledBorder.DEFAULT_POSITION,
        new Font("Arial", Font.BOLD, 16),
        Color.BLACK));
    JButton detailButton = new JButton("View Detail");
    detailButton.addActionListener(e -> {
      int selectedRow = loansTable.getSelectedRow();
      if (selectedRow >= 0) {
        String isbn = (String) loansTable.getValueAt(selectedRow, 0);
        List<Book> books = library.searchBooksByISBN(isbn);
        if (!books.isEmpty()) {
          showIssueDetail(books.get(0));
        }
      } else {
        JOptionPane.showMessageDialog(mainFrame,
            "Please select a loan record",
            "No Selection", JOptionPane.WARNING_MESSAGE);
      }
    });

    loansModel = new DefaultTableModel();
    DefaultTableModel loansModel = new DefaultTableModel();
    loansModel.addColumn("ISBN");
    loansModel.addColumn("Title");
    loansModel.addColumn("Borrower");
    loansModel.addColumn("Loan Date");
    loansModel.addColumn("Due Date");
    loansModel.addColumn("Status");
    loansTable = new JTable(loansModel);
    JTable loansTable = new JTable(loansModel);
    loansTable.setFont(new Font("Arial", Font.PLAIN, 15));
    loansTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
    loansTable.setRowHeight(25);

    // Add existing loan data
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    for (Book book : library.getAllBooks()) {
      if (book.isOnLoan() && book.getLoanDate() != null && book.getDueDate() != null) {
        loansModel.addRow(new Object[] {
            book.getIsbn(),
            book.getTitle(),
            book.getBorrower(),
            book.getLoanDate().format(formatter),
            book.getDueDate().format(formatter),
            "On Loan"
        });
      }
    }

    // Add return book button and action
    JPanel returnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton returnButton = new JButton("Return Book");
    returnButton.setFont(new Font("Arial", Font.PLAIN, 15));
    returnButton.setBackground(new Color(231, 76, 60));
    returnButton.setForeground(Color.BLACK);

    returnButton.addActionListener(e -> {
      int selectedRow = loansTable.getSelectedRow();
      if (selectedRow >= 0) {
        String isbn = (String) loansTable.getValueAt(selectedRow, 0);
        List<Book> books = library.searchBooksByISBN(isbn);
        Book book = books.isEmpty() ? null : books.get(0);

        if (book != null && book.isOnLoan()) {
          showReturnDetail(book);

          int confirm = JOptionPane.showConfirmDialog(mainFrame,
              "Confirm return this book?",
              "Return Confirmation", JOptionPane.YES_NO_OPTION);

          if (confirm == JOptionPane.YES_OPTION) {
            book.setOnLoan(false);
            book.setBorrower(null);

            try {
              library.saveBooksToFile("books.dat");
              loansModel.removeRow(selectedRow);
              refreshAllViews();
              bookCombo.addItem(book.getTitle() + " (" + book.getIsbn() + ")");
            } catch (IOException ex) {
              JOptionPane.showMessageDialog(mainFrame,
                  "Error saving data: " + ex.getMessage(),
                  "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      } else {
        JOptionPane.showMessageDialog(mainFrame,
            "Please select a book to return",
            "No Selection", JOptionPane.WARNING_MESSAGE);
      }
    });

    // Add refresh button
    JButton refreshButton = new JButton("Refresh");
    refreshButton.setFont(new Font("Arial", Font.PLAIN, 15));
    refreshButton.setBackground(new Color(52, 152, 219));
    refreshButton.setForeground(Color.BLACK);
    refreshButton.addActionListener(e -> refreshLoanTable(loansModel));

    returnPanel.add(detailButton);
    returnPanel.add(refreshButton);
    returnPanel.add(returnButton);

    historyPanel.add(new JScrollPane(loansTable), BorderLayout.CENTER);
    historyPanel.add(returnPanel, BorderLayout.SOUTH);

    // Add panels to split pane
    splitPane.setTopComponent(lendingFormPanel);
    splitPane.setBottomComponent(historyPanel);

    panel.add(splitPane, BorderLayout.CENTER);

    return panel;
  }

  private void refreshGalleryTab() {
    int index = tabbedPane.indexOfTab("Book Gallery");
    if (index != -1) {
      tabbedPane.setComponentAt(index, createGalleryPanel());
    }
  }

  private void refreshLoanTable(DefaultTableModel loansModel) {
    loansModel.setRowCount(0); // Delete old data
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    for (Book book : library.getAllBooks()) {
      if (book.isOnLoan()) {
        String formattedLoanDate = (book.getLoanDate() != null) ? book.getLoanDate().format(formatter) : "N/A";
        String formattedDueDate = (book.getDueDate() != null) ? book.getDueDate().format(formatter) : "N/A";

        loansModel.addRow(new Object[] {
            book.getIsbn(),
            book.getTitle(),
            book.getBorrower(),
            formattedLoanDate,
            formattedDueDate,
            "On Loan"
        });
      }
    }
  }

  private void returnBook() {
    int selectedRow = loansTable.getSelectedRow();
    if (selectedRow >= 0) {
      String isbn = (String) loansTable.getValueAt(selectedRow, 0);
      List<Book> books = library.searchBooksByISBN(isbn);
      Book book = books.isEmpty() ? null : books.get(0);

      if (book != null && book.isOnLoan()) {
        // add to history
        library.addLoanRecord(book);

        book.setOnLoan(false);
        book.setBorrower(null);

        try {
          library.saveBooksToFile("books.dat");
          refreshAllViews();
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(mainFrame,
              "Error saving data: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  /**
   * Create a card component for a book in the gallery view
   */
  private JPanel createBookCard(Book book) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setPreferredSize(new Dimension(200, 350));
    card.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(new Color(200, 200, 200), 1),
        new EmptyBorder(10, 10, 10, 10)));
    card.setBackground(Color.WHITE);

    // Book cover
    JLabel coverLabel = new JLabel() {
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ImageIcon icon = bookImages.get(book.getIsbn());
        if (icon != null) {
          g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
          g.setColor(Color.WHITE);
          g.fillRect(0, 0, getWidth(), getHeight());
          g.setColor(Color.GRAY);
          g.drawString("No Image", 10, 20);
        }

      };
    };
    coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    coverLabel.setPreferredSize(new Dimension(150, 200));
    coverLabel.setMaximumSize(new Dimension(150, 200));
    coverLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    // Set book cover image if available
    ImageIcon coverImage = bookImages.get(book.getIsbn());
    if (coverImage != null) {
      coverLabel.setIcon(coverImage);
    } else {
      // Default cover with title text
      coverLabel.setText(book.getTitle());
      coverLabel.setHorizontalAlignment(JLabel.CENTER);
      coverLabel.setVerticalAlignment(JLabel.CENTER);
      coverLabel.setFont(new Font("Arial", Font.BOLD, 15));
      coverLabel.setBackground(new Color(240, 240, 240));
      coverLabel.setOpaque(true);
    }

    // Book title
    JLabel titleLabel = new JLabel(book.getTitle(), JLabel.CENTER);
    titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 15));

    // Book author
    JLabel authorLabel = new JLabel("by " + book.getAuthor(), JLabel.CENTER);
    authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    authorLabel.setFont(new Font("Arial", Font.ITALIC, 12));

    // Book status
    JLabel statusLabel = new JLabel(book.isOnLoan() ? "On Loan" : "Available", JLabel.CENTER);
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    statusLabel.setForeground(book.isOnLoan() ? new Color(231, 76, 60) : new Color(46, 204, 113));
    statusLabel.setFont(new Font("Arial", Font.BOLD, 12));

    // View details button
    JButton viewButton = new JButton("View Details");
    viewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    viewButton.setFont(new Font("Arial", Font.PLAIN, 12));
    viewButton.setFocusPainted(false);

    viewButton.addActionListener(e -> {
      showBookDetailsDialog(book);
    });

    // Add components to card
    card.add(coverLabel);
    card.add(Box.createRigidArea(new Dimension(0, 10)));
    card.add(titleLabel);
    card.add(Box.createRigidArea(new Dimension(0, 5)));
    card.add(authorLabel);
    card.add(Box.createRigidArea(new Dimension(0, 10)));
    card.add(statusLabel);
    card.add(Box.createRigidArea(new Dimension(0, 10)));
    card.add(viewButton);

    return card;
  }

  /**
   * Show dialog with detailed book information
   */
  private void showBookDetailsDialog(Book book) {
    JDialog dialog = new JDialog(mainFrame, "Book Details", true);
    dialog.setSize(600, 400);
    dialog.setLocationRelativeTo(mainFrame);

    JPanel panel = new JPanel(new BorderLayout(20, 20));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Book cover panel
    JPanel coverPanel = new JPanel(new BorderLayout());
    JLabel coverLabel = new JLabel();
    coverLabel.setPreferredSize(new Dimension(150, 200));
    coverLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    // Edit image button
    JButton editImageButton = new JButton("Edit Image");
    editImageButton.addActionListener(e -> updateBookImage(book, dialog));

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());
    buttonPanel.add(editImageButton);
    buttonPanel.add(closeButton);

    // Set book cover image if available
    ImageIcon coverImage = bookImages.get(book.getIsbn());
    if (coverImage != null) {
      coverLabel.setIcon(coverImage);
    } else {
      // Default cover with title text
      coverLabel.setText(book.getTitle());
      coverLabel.setHorizontalAlignment(JLabel.CENTER);
      coverLabel.setFont(new Font("Arial", Font.BOLD, 15));
      coverLabel.setBackground(new Color(240, 240, 240));
      coverLabel.setOpaque(true);
    }

    coverPanel.add(coverLabel, BorderLayout.CENTER);

    // Book details panel
    JPanel detailsPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.3;

    // Title
    detailsPanel.add(new JLabel("Title:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    JLabel titleValue = new JLabel(book.getTitle());
    titleValue.setFont(new Font("Arial", Font.BOLD, 15));
    detailsPanel.add(titleValue, gbc);

    // Author
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.3;
    detailsPanel.add(new JLabel("Author:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    detailsPanel.add(new JLabel(book.getAuthor()), gbc);

    // ISBN
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.3;
    detailsPanel.add(new JLabel("ISBN:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    detailsPanel.add(new JLabel(book.getIsbn()), gbc);

    // Publication Year
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 0.3;
    detailsPanel.add(new JLabel("Publication Year:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    detailsPanel.add(new JLabel(String.valueOf(book.getPublicationYear())), gbc);

    // Book Type
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.weightx = 0.3;
    detailsPanel.add(new JLabel("Book Type:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;

    String typeText = "";
    if (book instanceof FictionBook) {
      typeText = "Fiction";
    } else if (book instanceof NonFictionBook) {
      typeText = "Non-Fiction";
    } else if (book instanceof ReferenceBook) {
      typeText = "Reference";
    }
    detailsPanel.add(new JLabel(typeText), gbc);

    // Type specific information
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.weightx = 0.3;

    String specificLabel = "";
    String specificValue = "";

    if (book instanceof FictionBook) {
      specificLabel = "Genre:";
      specificValue = ((FictionBook) book).getGenre();
    } else if (book instanceof NonFictionBook) {
      specificLabel = "Subject:";
      specificValue = ((NonFictionBook) book).getSubject();
    } else if (book instanceof ReferenceBook) {
      specificLabel = "Category:";
      specificValue = ((ReferenceBook) book).getCategory();
    }

    detailsPanel.add(new JLabel(specificLabel), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    detailsPanel.add(new JLabel(specificValue), gbc);

    // Availability
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.weightx = 0.3;
    detailsPanel.add(new JLabel("Status:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 0.7;

    // set color based on availability
    JLabel statusLabel = new JLabel(book.isOnLoan() ? "On Loan" : "Available");
    statusLabel.setForeground(book.isOnLoan() ? new Color(231, 76, 60) : new Color(46, 204, 113));
    statusLabel.setFont(new Font("Arial", Font.BOLD, 15));
    detailsPanel.add(statusLabel, gbc);

    // If on loan, show borrower
    if (book.isOnLoan()) {
      gbc.gridx = 0;
      gbc.gridy = 7;
      gbc.weightx = 0.3;
      detailsPanel.add(new JLabel("Borrowed By:"), gbc);
      gbc.gridx = 1;
      gbc.weightx = 0.7;
      detailsPanel.add(new JLabel(book.getBorrower()), gbc);
    }

    // Add panels to main panel
    panel.add(coverPanel, BorderLayout.WEST);
    panel.add(detailsPanel, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setContentPane(panel);
    dialog.setVisible(true);
  }

  // Update Image
  private void updateBookImage(Book book, JDialog parentDialog) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));

    int result = fileChooser.showOpenDialog(parentDialog);
    if (result == JFileChooser.APPROVE_OPTION) {
      try {
        // 1. Read image file
        File newImageFile = fileChooser.getSelectedFile();
        BufferedImage originalImage = ImageIO.read(newImageFile);

        // 2. Resize and convert to PNG
        BufferedImage resizedImage = new BufferedImage(150, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(150, 200, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        // 3. save new image
        File outputFile = new File("book_covers/" + book.getIsbn() + ".png");
        ImageIO.write(resizedImage, "png", outputFile);

        // 4. UI update
        SwingUtilities.invokeLater(() -> {
          bookImages.put(book.getIsbn(), new ImageIcon(resizedImage));
          refreshGalleryTab();
          parentDialog.dispose();
          showBookDetailsDialog(book); // Open dialog
        });

      } catch (Exception ex) {
        JOptionPane.showMessageDialog(parentDialog,
            "Error loading image: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void saveBookImage(String isbn, String imagePath) {
    try {
      File destDir = new File("book_covers");
      if (!destDir.exists())
        destDir.mkdir();

      // convert image to PNG
      ImageIcon icon = new ImageIcon(imagePath);
      Image image = icon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
      BufferedImage bufferedImage = new BufferedImage(150, 200, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = bufferedImage.createGraphics();
      g2d.drawImage(image, 0, 0, null);
      g2d.dispose();

      ImageIO.write(bufferedImage, "png", new File(destDir, isbn + ".png"));
    } catch (IOException ex) {
      System.err.println("Error when loading image" + ex.getMessage());
    }
  }

  /**
   * Refresh book table with current library data
   */
  private void refreshBookTable() {
    booksTableModel.setRowCount(0);

    for (Book book : library.getAllBooks()) {
      String typeStr;
      String detailsStr;

      if (book instanceof FictionBook) {
        typeStr = "Fiction";
        detailsStr = "Genre: " + ((FictionBook) book).getGenre();
      } else if (book instanceof NonFictionBook) {
        typeStr = "Non-Fiction";
        detailsStr = "Subject: " + ((NonFictionBook) book).getSubject();
      } else if (book instanceof ReferenceBook) {
        typeStr = "Reference";
        detailsStr = "Category: " + ((ReferenceBook) book).getCategory();
      } else {
        typeStr = "Unknown";
        detailsStr = "";
      }

      booksTableModel.addRow(new Object[] {
          book.getIsbn(),
          book.getTitle(),
          book.getAuthor(),
          book.getPublicationYear(),
          typeStr,
          detailsStr,
          book.isOnLoan() ? "On Loan" : "Available"
      });
    }
  }

  /**
   * Search books based on search field
   */
  private void searchBooks() {
    String searchTerm = searchField.getText().trim().toLowerCase();

    if (searchTerm.isEmpty()) {
      refreshAllViews();
      return;
    }

    booksTableModel.setRowCount(0);

    for (Book book : library.getAllBooks()) {
      if (book.getTitle().toLowerCase().contains(searchTerm) ||
          book.getAuthor().toLowerCase().contains(searchTerm) ||
          book.getIsbn().toLowerCase().contains(searchTerm)) {

        String typeStr;
        String detailsStr;

        if (book instanceof FictionBook) {
          typeStr = "Fiction";
          detailsStr = "Genre: " + ((FictionBook) book).getGenre();
        } else if (book instanceof NonFictionBook) {
          typeStr = "Non-Fiction";
          detailsStr = "Subject: " + ((NonFictionBook) book).getSubject();
        } else if (book instanceof ReferenceBook) {
          typeStr = "Reference";
          detailsStr = "Category: " + ((ReferenceBook) book).getCategory();
        } else {
          typeStr = "Unknown";
          detailsStr = "";
        }

        booksTableModel.addRow(new Object[] {
            book.getIsbn(),
            book.getTitle(),
            book.getAuthor(),
            book.getPublicationYear(),
            typeStr,
            detailsStr,
            book.isOnLoan() ? "On Loan" : "Available"
        });
      }
    }

  }

  private JDialog createEditBookDialog(Book book) {
    JDialog dialog = new JDialog(mainFrame, "Edit Book", true);
    dialog.setSize(600, 500);
    dialog.setLocationRelativeTo(mainFrame);
    dialog.setLayout(new BorderLayout(10, 10));

    // Main panel
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Form panel
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Book type
    JLabel typeLabel = new JLabel("Book Type:");
    typeLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JComboBox<String> typeCombo = new JComboBox<>(
        new String[] { "Fiction Book", "Non-Fiction Book", "Reference Book" });
    typeCombo.setFont(new Font("Arial", Font.PLAIN, 15));

    // ISBN
    JLabel isbnLabel = new JLabel("ISBN:");
    isbnLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField isbnField = new JTextField(20);
    isbnField.setFont(new Font("Arial", Font.PLAIN, 15));
    isbnField.setEditable(false); // ISBN should not be editable

    // Title
    JLabel titleLabel = new JLabel("Title:");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField titleField = new JTextField(20);
    titleField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Author
    JLabel authorLabel = new JLabel("Author:");
    authorLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField authorField = new JTextField(20);
    authorField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Year
    JLabel yearLabel = new JLabel("Publication Year:");
    yearLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField yearField = new JTextField(20);
    yearField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Specific field (Genre/Subject/Category)
    JLabel specificLabel = new JLabel();
    specificLabel.setFont(new Font("Arial", Font.BOLD, 15));
    JTextField specificField = new JTextField(20);
    specificField.setFont(new Font("Arial", Font.PLAIN, 15));

    // Update specific field label based on book type
    typeCombo.addActionListener(e -> {
      String selectedType = (String) typeCombo.getSelectedItem();
      if ("Fiction Book".equals(selectedType)) {
        specificLabel.setText("Genre:");
      } else if ("Non-Fiction Book".equals(selectedType)) {
        specificLabel.setText("Subject:");
      } else if ("Reference Book".equals(selectedType)) {
        specificLabel.setText("Category:");
      }
    });

    // Pre-fill data based on book type
    if (book instanceof FictionBook) {
      typeCombo.setSelectedItem("Fiction Book");
      specificLabel.setText("Genre:");
      specificField.setText(((FictionBook) book).getGenre());
    } else if (book instanceof NonFictionBook) {
      typeCombo.setSelectedItem("Non-Fiction Book");
      specificLabel.setText("Subject:");
      specificField.setText(((NonFictionBook) book).getSubject());
    } else if (book instanceof ReferenceBook) {
      typeCombo.setSelectedItem("Reference Book");
      specificLabel.setText("Category:");
      specificField.setText(((ReferenceBook) book).getCategory());
    }

    // Pre-fill other fields
    isbnField.setText(book.getIsbn());
    titleField.setText(book.getTitle());
    authorField.setText(book.getAuthor());
    yearField.setText(String.valueOf(book.getPublicationYear()));

    // Add components to form panel
    gbc.gridx = 0;
    gbc.gridy = 0;
    formPanel.add(typeLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(typeCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    formPanel.add(isbnLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(isbnField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    formPanel.add(titleLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(titleField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    formPanel.add(authorLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(authorField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    formPanel.add(yearLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(yearField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    formPanel.add(specificLabel, gbc);

    gbc.gridx = 1;
    formPanel.add(specificField, gbc);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton saveButton = new JButton("Save Changes");
    saveButton.setFont(new Font("Arial", Font.BOLD, 15));
    saveButton.setBackground(new Color(46, 204, 113));
    saveButton.setForeground(Color.BLACK);

    saveButton.addActionListener(e -> {
      // Validate and save changes
      String title = titleField.getText().trim();
      String author = authorField.getText().trim();
      String yearText = yearField.getText().trim();
      String specificValue = specificField.getText().trim();

      if (title.isEmpty() || author.isEmpty() || yearText.isEmpty() || specificValue.isEmpty()) {
        JOptionPane.showMessageDialog(dialog,
            "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      int year;
      try {
        year = Integer.parseInt(yearText);
      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(dialog,
            "Year must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Update book details
      book.setTitle(title);
      book.setAuthor(author);
      book.setYear(year);

      String selectedType = (String) typeCombo.getSelectedItem();
      switch (selectedType) {
        case "Fiction Book":
          ((FictionBook) book).setGenre(specificValue);
          break;
        case "Non-Fiction Book":
          ((NonFictionBook) book).setSubject(specificValue);
          break;
        case "Reference Book":
          ((ReferenceBook) book).setCategory(specificValue);
          break;
        default:
          throw new IllegalArgumentException("Invalid book type: " + selectedType);
      }

      try {
        library.saveBooksToFile("books.dat");
        refreshBookTable();
        dialog.dispose();
        JOptionPane.showMessageDialog(mainFrame,
            "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(dialog,
            "Error saving changes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setFont(new Font("Arial", Font.PLAIN, 15));
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    // Add components to main panel
    mainPanel.add(formPanel, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    dialog.add(mainPanel);
    return dialog;
  }

  public static void main(String[] args) {

    // set Look and Feel
    setUIFont(new javax.swing.plaf.FontUIResource("Arial", Font.PLAIN, 15));
    SwingUtilities.invokeLater(() -> {
      new guiFinal();
    });
  }

  // setUIFont
  public static void setUIFont(javax.swing.plaf.FontUIResource f) {
    java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource) {
        UIManager.put(key, f);
      }
    }
  }
}
