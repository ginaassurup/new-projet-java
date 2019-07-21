/*
 * In - MainWindow class
 */

package view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import dao.SQLiteCon;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Toolkit;

import model.Categorie;
import model.MyRenderer;
import model.ProduitDetail;
import model.ProductJoin;
import model.ProductJoinTableModel;
import model.ProductTableModel;

import java.awt.event.KeyAdapter;
import java.awt.Font;

import javax.swing.ListSelectionModel;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.JSeparator;
import javax.swing.ImageIcon;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// first category in combo box
	String firstCatStr = "Toutes";

	// selected product
	String currentProductName;

	// current product search string
	String currentProductSearch;

	// current product search result
	List<ProductJoin> currentListProductJoin;

	// AddProduct Window declaration
	AjouterUnProduitFenetre ajouterUnProduitFenetre;

	// EditProduct Window declaration
	EditProductWindow editProductWindow;

	// Categories Window declaration
	CategoriesWindow categoriesWindow;

	// Units Window declaration
	FournisseursFenetre fournisseursFenetre;

	// database class declaration
	SQLiteCon conn;

	// fields, buttons, tables that need access
	JTable tableProduct;
	private JComboBox<String> comboBoxCategory;
	private JTextField textFieldSearch;
	private JButton buttonPlus;
	private JButton buttonMinus;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "static-access" })
	public MainWindow() {
		
		// initialise connection
		conn = new SQLiteCon();

		createMenuBar();
		setResizable(false);

		setTitle("Produits | Utilisateur : " + conn.currentUser);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 968, 700);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(new Color(163, 193, 228));
		scrollPane.setBounds(29, 156, 906, 383);
		contentPane.add(scrollPane);

		tableProduct = new JTable() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void changeSelection(int rowIndex, int columnIndex,
					boolean toggle, boolean extend) {
				// Always toggle on single selection
				super.changeSelection(rowIndex, columnIndex, !extend, extend);
			}
		};
		tableProduct.setFillsViewportHeight(true);
		tableProduct.setBackground(SystemColor.window);
		tableProduct.setSelectionBackground(new Color(163, 193, 228));
		tableProduct.setRequestFocusEnabled(false);

		tableProduct.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(tableProduct);

		comboBoxCategory = new JComboBox(getCategoriesToCombo());

		// combobox highlighter color
		Object child = comboBoxCategory.getAccessibleContext().getAccessibleChild(0);
		BasicComboPopup popup = (BasicComboPopup)child;
		JList list = popup.getList();
		list.setSelectionBackground(new Color(204, 204, 204));
		
		
		comboBoxCategory.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {

				categoryFilter(evt);
			}
		});
		comboBoxCategory.setMaximumRowCount(20);
		comboBoxCategory.setBounds(29, 95, 125, 30);
		contentPane.add(comboBoxCategory);

		JButton btnCategories = new JButton("Cat\u00E9gorie");
		btnCategories.setFont(new Font("Tahoma", Font.PLAIN, 10));
		//btnCategories.setBackground(new Color(75, 190, 95));
		btnCategories.setBackground(new Color(204, 204, 204));
		btnCategories.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openCategories();
			}
		});
		btnCategories.setFocusPainted(false);
		btnCategories.setBounds(156, 95, 82, 30);
		contentPane.add(btnCategories);

		textFieldSearch = new JTextField();

		textFieldSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					searchBtn();
				}
			}
		});
		textFieldSearch.setToolTipText("ProduitDetail Name");
		textFieldSearch.setBounds(537, 95, 118, 30);
		contentPane.add(textFieldSearch);
		textFieldSearch.setColumns(10);

		// request focus
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textFieldSearch.requestFocusInWindow();
			}
		});

		JButton btnSearch = new JButton("Rechercher");
		btnSearch.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnSearch.setBackground(new Color(204, 204, 204));
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				searchBtn();
			}
		});
		btnSearch.setFocusPainted(false);
		btnSearch.setBounds(692, 95, 118, 30);
		contentPane.add(btnSearch);

		JLabel lblNewLabel = new JLabel("Produit");
		lblNewLabel.setForeground(Color.LIGHT_GRAY);
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(435, 101, 97, 14);
		contentPane.add(lblNewLabel);

		JButton btnShowAll = new JButton("Tout afficher");
		btnShowAll.setBackground(new Color(204, 204, 204));
		btnShowAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				showwAll();
			}
		});
		btnShowAll.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnShowAll.setFocusPainted(false);
		btnShowAll.setBounds(822, 94, 113, 30);
		contentPane.add(btnShowAll);

		JLabel lblProducts = new JLabel("Gestion des produits");
		lblProducts.setForeground(Color.LIGHT_GRAY);
		lblProducts.setHorizontalAlignment(SwingConstants.CENTER);
		lblProducts.setBounds(30, 558, 244, 14);
		contentPane.add(lblProducts);

		JLabel lblStock = new JLabel("Stock");
		lblStock.setForeground(Color.LIGHT_GRAY);
		lblStock.setHorizontalAlignment(SwingConstants.CENTER);
		lblStock.setBounds(782, 558, 75, 14);
		contentPane.add(lblStock);
		
		JLabel labelLogo = new JLabel("");
		labelLogo.setIcon(null);
		labelLogo.setBounds(319, 516, 64, 64);
		contentPane.add(labelLogo);
		
		JLabel lblProduits = new JLabel("Produits");
		lblProduits.setHorizontalAlignment(SwingConstants.CENTER);
		lblProduits.setForeground(new Color(165, 42, 42));
		lblProduits.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblProduits.setBounds(218, 57, 498, 25);
		contentPane.add(lblProduits);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 962, 30);
		contentPane.add(menuBar);
		
		JMenu menu = new JMenu("Param\u00E8tres");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.setFont(new Font("Tahoma", Font.PLAIN, 15));
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("Imprimer");
		menu.add(menuItem);
		
		JSeparator separator = new JSeparator();
		menu.add(separator);
		
		JMenuItem menuItem_1 = new JMenuItem("Fermer");
		menuItem_1.setToolTipText("Exit application");
		menuItem_1.setMnemonic(KeyEvent.VK_E);
		menu.add(menuItem_1);
		
		JMenu menu_1 = new JMenu("");
		menu_1.setMnemonic(KeyEvent.VK_F);
		menu_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
		menuBar.add(menu_1);
		
		JSeparator separator_1 = new JSeparator();
		menu_1.add(separator_1);

		JButton btnAddProduct = new JButton("Ajouter");
		btnAddProduct.setBounds(29, 586, 80, 30);
		contentPane.add(btnAddProduct);
		btnAddProduct.setBackground(new Color(204, 204, 204));
		btnAddProduct.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				addProduct();
			}
		});
		btnAddProduct.setFont(new Font("Tahoma", Font.PLAIN, 10));
				
		btnAddProduct.setFocusPainted(false);
						
		JButton btnRemoveProduct = new JButton("Supprimer");
		btnRemoveProduct.setBounds(111, 586, 80, 30);
		contentPane.add(btnRemoveProduct);
		btnRemoveProduct.setBackground(new Color(204, 204, 204));
		btnRemoveProduct.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				removeProduct();
			}
		});
		
		btnRemoveProduct.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnRemoveProduct.setFocusPainted(false);
	
		JButton btnEdit = new JButton("Modifier");
		btnEdit.setBounds(193, 586, 80, 30);
		contentPane.add(btnEdit);
		btnEdit.setBackground(new Color(204, 204, 204));
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// edit product
				editProduct();

			}
		});
				
		btnEdit.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnEdit.setFocusPainted(false);
		
		buttonPlus = new JButton("+");
		buttonPlus.setBounds(810, 586, 125, 30);
		contentPane.add(buttonPlus);
		buttonPlus.setBackground(new Color(204, 204, 204));
		buttonPlus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addStock();
			}
		});
		
		buttonPlus.setFocusPainted(false);
		
		buttonMinus = new JButton("-");
		buttonMinus.setBounds(683, 586, 125, 30);
		contentPane.add(buttonMinus);
		buttonMinus.setBackground(new Color(204, 204, 204));
		buttonMinus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeStock();
			}
		});
		buttonMinus.setFocusPainted(false);
				
		JButton btnRetour = new JButton("Retour");
		btnRetour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				OuvrirMenuPrincipal();
			}
		});
		btnRetour.setBounds(10, 42, 97, 25);
		contentPane.add(btnRetour);
		setLocationRelativeTo(null);

		getProductsJoin();
		// refreshTable();
	}

	// Ouvrir le menu principal
	private void OuvrirMenuPrincipal() {
		
		MenuPrincipal menuPrincipal = new MenuPrincipal();
		menuPrincipal.getFrmMenuPrincipal().setVisible(true);
	}

	// menu bar
	private void createMenuBar() {
	}

	// opens Categories Window
	private void openCategories() {
		categoriesWindow = new CategoriesWindow();
		categoriesWindow.setVisible(true);
		while (categoriesWindow.isShowing()) {
			//
		}
		refreshComboBox();
		getProductsJoin();
	}

	/*
	 * Get data to the table and combobox
	 */

	// get all products to the table (join table query)
	private void getProductsJoin() {

		try {
			List<ProductJoin> productsJoin = null;
			productsJoin = conn.getProductsJoin();
			ProductJoinTableModel model = new ProductJoinTableModel(
					productsJoin);
			tableProduct.setModel(model);

			
//			hideProductIdColumn();
//			hideStockAlarmColumn();
			allignColumn();
			colourIfStockAlarm();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// hides product id column
	private void hideProductIdColumn() {

		TableColumn productIdColumn = tableProduct.getColumnModel()
				.getColumn(0);
		// tableCategories.getColumnModel().removeColumn(myTableColumn0);
		productIdColumn.setMaxWidth(0);
		productIdColumn.setMinWidth(0);
		productIdColumn.setPreferredWidth(0);

	}

	// hides stockAlarm column
	private void hideStockAlarmColumn() {

		// hides stockAlarm column
		TableColumn stockAlarmColumn = tableProduct.getColumnModel().getColumn(
				6);
		// tableCategories.getColumnModel().removeColumn(myTableColumn0);
		stockAlarmColumn.setMaxWidth(0);
		stockAlarmColumn.setMinWidth(0);
		stockAlarmColumn.setPreferredWidth(0);

	}

	// allignment
	private void allignColumn() {
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		tableProduct.getColumnModel().getColumn(4)
				.setCellRenderer(leftRenderer);
	}

	// change colour if stockAlarm
	private void colourIfStockAlarm() {
	
		MyRenderer colorRenderer = new MyRenderer();
		tableProduct.getColumnModel().getColumn(4).setCellRenderer(colorRenderer);	
	}

	// get all categories to comboBox
	private String[] getCategoriesToCombo() {

		try {
			List<Categorie> categories = null;
			ArrayList<String> comboCategories = new ArrayList<String>();
			comboCategories.add("Toutes");
			categories = conn.getAllCategories();

			for (int i = 0; i < categories.size(); i++) {
				comboCategories.add(categories.get(i).getNom_cat());
				System.out.println(comboCategories.get(i));
			}

			return comboCategories.toArray(new String[comboCategories.size()]);

		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	// refreshes combobox after change
	public void refreshComboBox() {

		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				@SuppressWarnings({ "rawtypes" })
				DefaultComboBoxModel model = new DefaultComboBoxModel(
						getCategoriesToCombo());
				comboBoxCategory.setModel(model);
				comboBoxCategory.setSelectedItem(firstCatStr);
			}
		});
	}

	/*
	 * ProduitDetail search and category filter
	 */

	// search button method
	private void searchBtn() {

		try {
			String product = textFieldSearch.getText();
			currentProductSearch = product;
			List<ProductJoin> productsJoin = null;

			if (product != null && product.trim().length() > 0) {
				productsJoin = conn.searchProductsJoinCat(product, firstCatStr);
			} else {
				productsJoin = conn.searchProductsJoinCat("", firstCatStr);
			}

			for (ProductJoin temp : productsJoin) {
				System.out.println(temp);
			}

			ProductJoinTableModel model = new ProductJoinTableModel(
					productsJoin);

			tableProduct.setModel(model);
//			hideProductIdColumn();
//			hideStockAlarmColumn();
			allignColumn();
			colourIfStockAlarm();
			currentListProductJoin = productsJoin;

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// show all method
	private void showwAll() {
		textFieldSearch.setText("");
		searchBtn();
	}

	// filters category depending on item in combobox
	private void categoryFilter(ItemEvent evt) {
		Object item = evt.getItem();

		if (evt.getStateChange() == ItemEvent.SELECTED) {
			// Item was just selected
			try {

				firstCatStr = item.toString();

				System.out.println(firstCatStr);

				List<ProductJoin> productsJoin = null;

				if (firstCatStr.equalsIgnoreCase("Toutes")) {
					productsJoin = conn.getProductsJoin();
				} else {
					productsJoin = conn.filterProductsByCat(firstCatStr);
				}

				for (ProductJoin temp : productsJoin) {
					System.out.println(temp);
				}

				ProductJoinTableModel model = new ProductJoinTableModel(
						productsJoin);
				tableProduct.setModel(model);
//				hideProductIdColumn();
//				hideStockAlarmColumn();
				allignColumn();
				colourIfStockAlarm();
				currentListProductJoin = productsJoin;
				textFieldSearch.setText("");

			} catch (Exception e) {
				// TODO: handle exception
			}

		} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
			// Item is no longer selected
		}
	}

	/*
	 * Add and remove stock
	 */

	// add stock method
	@SuppressWarnings("static-access")
	private void addStock() {
		int prodIdCol = 0;
		int prodCol = 1;

		// if row selected
		if (!(tableProduct.getSelectedRow() == -1)) {
			int selectedRow = tableProduct.getSelectedRow();

			String prodId = tableProduct.getValueAt(selectedRow, prodIdCol)
					.toString().trim();
			String prodName = tableProduct.getValueAt(selectedRow, prodCol)
					.toString().trim();

			boolean numeric = false;
			int quantity = 0;

			JOptionPane inpOption = new JOptionPane();
			String strDialogResponse = "";

			do {
				// Shows a inputdialog
				strDialogResponse = inpOption
						.showInputDialog("How much do you want to add: ");
				// if OK is pushed then (if not strDialogResponse is null)
				if (strDialogResponse != null) {

					try {
						quantity = Integer.parseInt(strDialogResponse.trim());
						numeric = true;
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"Please enter numeric value.");
						numeric = false;
					}
				}// If cancel button is pressed
				else {
					break;
				}
			} while (!numeric);

			try {
				conn.addStockQuery(prodId, prodName, quantity);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// refresh view here
			refreshTable();
		} else {

			JOptionPane.showMessageDialog(null,
					"In order to change the stock please select product first.");
		}
	}

	// remove stock method
	@SuppressWarnings("static-access")
	private void removeStock() {
		int prodIdCol = 0;
		int prodCol = 1;
		int prodStockCol = 4;
		int prodStockAlarmCol = 6;

		// if row selected
		if (!(tableProduct.getSelectedRow() == -1)) {

			int selectedRow = tableProduct.getSelectedRow();

			String prodId = tableProduct.getValueAt(selectedRow, prodIdCol)
					.toString().trim();
			String prodName = tableProduct.getValueAt(selectedRow, prodCol)
					.toString().trim();

			String prodStock = tableProduct
					.getValueAt(selectedRow, prodStockCol).toString().trim();

			String prodStockAlarm = tableProduct
					.getValueAt(selectedRow, prodStockAlarmCol).toString()
					.trim();

			boolean numeric = false;
			int quantity = 0;
			JOptionPane inpOption = new JOptionPane();
			String strDialogResponse = "";

			do {
				// Shows a input dialog
				strDialogResponse = inpOption
						.showInputDialog("How much do you want to remove?: ");
				// if OK is pushed then (if not strDialogResponse is null)
				if (strDialogResponse != null) {

					try {
						quantity = Integer.parseInt(strDialogResponse);
						numeric = true;
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,
								"Please enter numeric value.");
						numeric = false;
					}
				}// If cancel button is pressed
				else {
					break;
				}
			} while (!numeric);

			try {
				conn.removeStockQuery(prodId, prodName, quantity, prodStock,
						prodStockAlarm);

			} catch (Exception e) {
				e.printStackTrace();
			}
			// refresh view
			refreshTable();
		} else {
			System.out.println("Nothing selected");
			JOptionPane.showMessageDialog(null,
					"In order to change the stock please select product first.");
		}
	}

	/*
	 * Add and Remove product
	 */

	// add product
	private void addProduct() {

		// initialise AjouterUnProduitFenetre
		ajouterUnProduitFenetre = new AjouterUnProduitFenetre();
		dispose();
		ajouterUnProduitFenetre.setVisible(true);
		ajouterUnProduitFenetre.textFieldName.setText("");
		ajouterUnProduitFenetre.textFieldType.setText("");
		ajouterUnProduitFenetre.textFieldStock.setText("");

		while (ajouterUnProduitFenetre.isVisible()) {

		}

		refreshTable();
		refreshComboBox();
	}

	// Supprimer un produit
	private void removeProduct() {
		int prodIdCol = 0;
		int prodNameCol = 1;

		// if row selected
		if (!(tableProduct.getSelectedRow() == -1)) {

			int selectedRow = tableProduct.getSelectedRow();

			String num_prod = tableProduct.getValueAt(selectedRow, prodIdCol)
					.toString().trim();

			String code_barre = tableProduct.getValueAt(selectedRow, prodNameCol)
					.toString().trim();

			int reply = JOptionPane.showConfirmDialog(null,
					"Voulez-vous vraiment supprimer ce produit ?", "Remove ?",
					JOptionPane.YES_NO_OPTION);
			if (reply == JOptionPane.YES_OPTION) {

				try {
					conn.removeProductQuery(num_prod, code_barre);

				} catch (Exception e) {
					e.printStackTrace();
				}

				// refresh view here
				refreshTable();

				JOptionPane.showMessageDialog(null, "ProduitDetail removed.");

			} else {
				// do nothing
			}

		} else {
			System.out.println("Nothing selected");
			JOptionPane
					.showMessageDialog(null,
							"In order to remove product please select product row first.");
		}

	}

	// edit product
	private void editProduct() {
		if (!(tableProduct.getSelectedRow() == -1)) {
			editProductWindow = new EditProductWindow();

			int idCol = 0;
			int nameCol = 1;
			int catCol = 2;
			int typeCol = 3;
			int stockCol = 4;
			int unitCol = 5;
			int stockAlarmCol = 6;

			int selectedRow = tableProduct.getSelectedRow();

			System.out.println(tableProduct.getValueAt(selectedRow, idCol));
			
			String idStr = "" +tableProduct.getValueAt(selectedRow, idCol);
			
			editProductWindow.currentId = idStr;
			editProductWindow.textFieldName.setText(tableProduct
					.getValueAt(selectedRow, nameCol).toString().trim());
			editProductWindow.comboBoxCategory.setSelectedItem(tableProduct
					.getValueAt(selectedRow, catCol));
			editProductWindow.textFieldType.setText(tableProduct
					.getValueAt(selectedRow, typeCol).toString().trim());
			editProductWindow.textFieldStock.setText(tableProduct
					.getValueAt(selectedRow, stockCol).toString().trim());
			editProductWindow.comboBoxUnits.setSelectedItem(tableProduct
					.getValueAt(selectedRow, unitCol));

			editProductWindow.textFieldStockAlarm.setText(tableProduct
					.getValueAt(selectedRow, stockAlarmCol).toString().trim());

			currentProductName = editProductWindow.textFieldName.getText()
					.toString().trim();

			editProductWindow.currentProductName = currentProductName;
			editProductWindow.currentTypeName = tableProduct.getValueAt(
					selectedRow, typeCol).toString();

			editProductWindow.setVisible(true);
			while (editProductWindow.isVisible()) {

			}

			refreshTable();
			refreshComboBox();
		} else {
			JOptionPane.showMessageDialog(null,
					"In order to edit product please select product row first.");
		}
	}

	/*
	 * Other methods
	 */

	// method that refreshes table after changing stock
	public void refreshTable() {

		try {
			if (firstCatStr.equalsIgnoreCase("Toutes")) {
				currentListProductJoin = conn.getProductsJoin();
			} else {
				currentListProductJoin = conn.filterProductsByCat(firstCatStr);
			}

			if (currentProductSearch != null
					&& currentProductSearch.trim().length() > 0) {
				currentListProductJoin = conn.searchProductsJoinCat(
						currentProductSearch, firstCatStr);
			} else {

				currentListProductJoin = conn.searchProductsJoinCat("",
						firstCatStr);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ProductJoinTableModel model = new ProductJoinTableModel(
				currentListProductJoin);

		tableProduct.setModel(model);
//		hideProductIdColumn();
//		hideStockAlarmColumn();
		allignColumn();
		colourIfStockAlarm();
	}

	// //////////////////////
	// /DEPRECATED METHODS///
	// //////////////////////

	// get all products to table
	public void getProducts() {

		try {

			List<ProduitDetail> produitDetails = null;

			produitDetails = conn.getAllProducts();

			ProductTableModel model = new ProductTableModel(produitDetails);
			tableProduct.setModel(model);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
