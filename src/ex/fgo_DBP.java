package ex;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class fgo_DBP extends JFrame implements ActionListener, ItemListener {
	Panel searchPanel, nameSearchPanel, eventSearchPanel;
	Panel namePanel, rarityPanel, eventPanel, newEventPanel, rePanel, effectPanel, buttonPanel;
	/*a検索*/
	TextField nameSearchField, eventSearchField;
	Button nameSearchButton, eventSearchButton;
	JComboBox<String> eventSearchBox;
	ArrayList<String> eventList = new ArrayList<>();
	String nameSearchCommand = "名前検索",
			eventSearchCommand = "イベント検索";
	/*aリスト*/
	java.awt.List searchList;
	/*aカード画像*/
	JLabel imageLabel;
	/*aデータ詳細表示、操作*/
	Panel dataPanel;
	TextField nameField, rarityField, eventField, effectField;
	JComboBox<String> rarityBox, eventBox, reBox;
	String[] reStr = { "復刻済", "未復刻", "復刻無" };
	Button displayButton, updateButton, addButton, deleteButton;
	String[] rarityStr = { "★★★", "★★★★", "★★★★★" };
	String displayCommand = "再表示", updateCommand = "更新",
			addCommand = "追加", deleteCommand = "削除";

	/*psql*/
	String driverClassName = "org.postgresql.Driver";
	String url = "jdbc:postgresql://localhost/test";
	String user = "dbpuser";
	String password = "hogehoge";
	Connection connection;
	ResultSet resultSet;

	PreparedStatement prepStmt_SD;//SELECT DISTINCT event 用
	PreparedStatement prepStmt_SN;//SELECT name 用(リスト表示)
	PreparedStatement prepStmt_SE;//SELECT event 用(リスト表示)
	PreparedStatement prepStmt_S;//SELECT用
	PreparedStatement prepStmt_U;//UPDATE用
	PreparedStatement prepStmt_I;//INSERT用
	PreparedStatement prepStmt_D;//DELETE用

	String selectDis = "SELECT DISTINCT event FROM reiso";
	String selectStr = "SELECT name FROM reiso WHERE name LIKE ?";
	String selectEve = "SELECT name FROM reiso WHERE event = ?";
	String selectS = "SELECT * FROM reiso WHERE name = ?";
	String updateSQL = "UPDATE reiso SET rarity = ?, event = ?, re = ?, effect = ? WHERE name = ?";
	String insertSQL = "INSERT INTO reiso VALUES(?, ?, ?, ?, ?, ?)";
	String deleteSQL = "DELETE FROM reiso WHERE name = ?";

	public fgo_DBP() {
		//aウィンドウを作成する
		setSize(1000, 450);
		setTitle("fgo_イベント概念礼装管理");
		setLayout(new GridLayout(1, 4));

		try { // ドライバマネージャとコネクション
			Class.forName(driverClassName);
			connection = DriverManager.getConnection(url, user, password);

			prepStmt_SD = connection.prepareStatement(selectDis);
			prepStmt_SN = connection.prepareStatement(selectStr);
			prepStmt_SE = connection.prepareStatement(selectEve);
			prepStmt_S = connection.prepareStatement(selectS);
			prepStmt_U = connection.prepareStatement(updateSQL);
			prepStmt_I = connection.prepareStatement(insertSQL);
			prepStmt_D = connection.prepareStatement(deleteSQL);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*a名前検索とイベント名検索パネル生成*/
		searchPanel = new Panel(new GridLayout(2, 1, 0, 50));

		nameSearchPanel = new Panel(new GridLayout(3, 1));
		nameSearchPanel.add(new JLabel("名前で検索", JLabel.CENTER));//１列目：名前検索ラベル
		nameSearchField = new TextField();//2列目：名前入力テキストフィールド
		nameSearchPanel.add(nameSearchField);
		nameSearchButton = new Button(nameSearchCommand);//3列目：検索ボタン
		nameSearchButton.addActionListener(this);//押されたら反応する
		nameSearchPanel.add(nameSearchButton);
		searchPanel.add(nameSearchPanel);

		eventSearchPanel = new Panel(new GridLayout(3, 1));
		eventSearchPanel.add(new JLabel("イベント名で検索", JLabel.CENTER));
		eventSearchBox = new JComboBox<String>();
		eventSearchPanel.add(eventSearchBox);
		eventSearchButton = new Button(eventSearchCommand);
		eventSearchButton.addActionListener(this);
		eventSearchPanel.add(eventSearchButton);
		searchPanel.add(eventSearchPanel);

		add(searchPanel);

		/*a検索結果リスト生成*/
		searchList = new List(10);
		searchList.addItemListener(this);
		add(searchList);

		/*aカード画像表示パネル生成*/
		Icon imageIcon = new ImageIcon("");
		imageLabel = new JLabel(imageIcon);
		add(imageLabel);

		/*aデータ詳細、操作*/
		dataPanel = new Panel(new GridLayout(7, 1));

		namePanel = new Panel(new GridLayout(2, 1));
		namePanel.add(new Label("名前"));
		nameField = new TextField();
		namePanel.add(nameField);
		dataPanel.add(namePanel);

		rarityPanel = new Panel(new GridLayout(2, 1));
		rarityPanel.add(new Label("レア度"));
		rarityBox = new JComboBox<String>(rarityStr);
		rarityPanel.add(rarityBox);
		dataPanel.add(rarityPanel);

		eventPanel = new Panel(new GridLayout(2, 1));
		eventPanel.add(new Label("イベント名"));
		eventBox = new JComboBox<String>();
		eventPanel.add(eventBox);
		dataPanel.add(eventPanel);

		newEventPanel = new Panel(new GridLayout(2, 1));
		newEventPanel.add(new JLabel("新規イベント名"));
		eventField = new TextField();
		newEventPanel.add(eventField);
		dataPanel.add(newEventPanel);

		rePanel = new Panel(new GridLayout(2, 1));
		rePanel.add(new Label("復刻"));
		reBox = new JComboBox<String>(reStr);
		rePanel.add(reBox);
		dataPanel.add(rePanel);

		effectPanel = new Panel(new GridLayout(2, 1));
		effectPanel.add(new Label("効果"));
		effectField = new TextField();
		effectPanel.add(effectField);
		dataPanel.add(effectPanel);

		buttonPanel = new Panel(new GridLayout(2, 2));
		displayButton = new Button(displayCommand);
		displayButton.addActionListener(this);
		buttonPanel.add(displayButton);
		updateButton = new Button(updateCommand);
		updateButton.addActionListener(this);
		buttonPanel.add(updateButton);
		addButton = new Button(addCommand);
		addButton.addActionListener(this);
		buttonPanel.add(addButton);
		deleteButton = new Button(deleteCommand);
		deleteButton.addActionListener(this);
		buttonPanel.add(deleteButton);
		dataPanel.add(buttonPanel);

		add(dataPanel);

		//aウィンドウを閉じる処理
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				try { // 後処理
					prepStmt_SD.close();
					prepStmt_SN.close();
					prepStmt_SE.close();
					prepStmt_S.close();
					prepStmt_U.close();
					prepStmt_I.close();
					prepStmt_D.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.exit(0);
			}
		});

		setEventList();

	}//end of コンストラクタ

	@Override
	public void itemStateChanged(ItemEvent e) {//aリストから項目が選択された時の処理
		displayData();
	}

	public void clearList() {//a表示されているリストをクリア
		searchList.removeAll();
	}

	public void displayListFromName() {//a検索された文字を含む名前リスト項目を表示
		String str = nameSearchField.getText();//a入力された文字
		try {
			prepStmt_SN.setString(1, "%" + str + "%");
			resultSet = prepStmt_SN.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				searchList.add(name);
			}
			resultSet.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayListFromEvent() {//aイベント名で検索し、名前をリスト項目として表示
		String selectedEventName = (String) eventSearchBox.getSelectedItem();
		try {
			prepStmt_SE.setString(1, selectedEventName);
			resultSet = prepStmt_SE.executeQuery();
			while (resultSet.next()) {
				String name = resultSet.getString("name");
				searchList.add(name);
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setEventList() {//aイベント名リストを取得してJComboBoxにセットするメソッド
		try {
			resultSet = prepStmt_SD.executeQuery();//イベント名抜き出す
			eventList.clear();//eventListをクリア
			eventSearchBox.removeAllItems();//JComboBoxも一度クリア
			eventBox.removeAllItems();
			while (resultSet.next()) {
				String eventName = resultSet.getString("event");
				eventList.add(eventName);
			}
			resultSet.close();
			for (String eve : eventList) {
				eventSearchBox.addItem(eve);
				eventBox.addItem(eve);
			}
			eventBox.addItem("新規イベント");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayData() {//aリストで選択された礼装の詳細を表示
		String name = searchList.getSelectedItem();
		String image = "", rarity = "", event = "", re = "", effect = "";
		try {
			prepStmt_S.setString(1, name);
			resultSet = prepStmt_S.executeQuery();
			while (resultSet.next()) {
				name = resultSet.getString("name");
				image = resultSet.getString("image");
				image = "img/"+image;
				rarity = resultSet.getString("rarity");
				event = resultSet.getString("event");
				re = resultSet.getString("re");
				effect = resultSet.getString("effect");
			}
			nameField.setText(name);//a名前
			Icon imageIcon = new ImageIcon(image);//a画像
			imageLabel.setIcon(imageIcon);
			if (rarity.equals("3")) {//aレア度
				rarityBox.setSelectedIndex(0);
			} else if (rarity.equals("4")) {
				rarityBox.setSelectedIndex(1);
			} else if (rarity.equals("5")) {
				rarityBox.setSelectedIndex(2);
			}
			eventBox.setSelectedItem(event);//aイベント
			reBox.setSelectedItem(re);//a復刻
			effectField.setText(effect);//a効果

			resultSet.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateData() {//aデータ更新
		String name = searchList.getSelectedItem();
		String rarity = "", event = "", re = "", effect = "";
		nameField.setText(name);
		if (rarityBox.getSelectedIndex() == 0) {
			rarity = "3";
		} else if (rarityBox.getSelectedIndex() == 1) {
			rarity = "4";
		} else if (rarityBox.getSelectedIndex() == 2) {
			rarity = "5";
		}
		event = (String) eventBox.getSelectedItem();
		re = (String) reBox.getSelectedItem();
		effect = effectField.getText();
		try {
			prepStmt_U.setString(1, rarity);
			prepStmt_U.setString(2, event);
			prepStmt_U.setString(3, re);
			prepStmt_U.setString(4, effect);
			prepStmt_U.setString(5, name);
			prepStmt_U.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addData() {//aデータ追加
		String name = "";
		String image = "";
		String rarity = "", event = "", re = "", effect = "";
		name = nameField.getText();
		image = "img/" + name + ".jpg";
		if (rarityBox.getSelectedIndex() == 0) {
			rarity = "3";
		} else if (rarityBox.getSelectedIndex() == 1) {
			rarity = "4";
		} else if (rarityBox.getSelectedIndex() == 2) {
			rarity = "5";
		}
		if ((boolean) eventBox.getSelectedItem().equals("新規イベント")) {
			event = eventField.getText();
			eventBox.addItem(event);
			eventSearchBox.addItem(event);
		} else {
			event = (String) eventBox.getSelectedItem();
		}
		re = (String) reBox.getSelectedItem();
		effect = effectField.getText();
		try {
			prepStmt_I.setString(1, name);
			prepStmt_I.setString(2, image);
			prepStmt_I.setString(3, rarity);
			prepStmt_I.setString(4, event);
			prepStmt_I.setString(5, re);
			prepStmt_I.setString(6, effect);
			prepStmt_I.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteData() {//aデータ削除
		String name = searchList.getSelectedItem();
		try {//a名前の行を削除
			prepStmt_D.setString(1, name);
			prepStmt_D.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		clearList();
	}

	@Override
	//aボタンが押された時に行う処理
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();//コマンドを取得

		if (command.equals(nameSearchCommand)) {//a名前検索ボタン
			clearList();
			displayListFromName();
			setEventList();
		} else if (command.equals(eventSearchCommand)) {
			clearList();
			displayListFromEvent();
			setEventList();
		} else if (command.equals(displayCommand)) {
			displayData();
		} else if (command.equals(updateCommand)) {
			updateData();
			setEventList();
		} else if (command.equals(addCommand)) {
			addData();
			setEventList();
		} else if (command.equals(deleteCommand)) {
			deleteData();
			setEventList();
		}
	}

	public static void main(String[] args) {
		fgo_DBP fgo = new fgo_DBP();
		fgo.setVisible(true);
	}

}