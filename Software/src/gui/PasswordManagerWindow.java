package gui;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import main.Client;
import main.Password;
import main.Server;

public class PasswordManagerWindow {

	@FXML	private AnchorPane ap1;
	@FXML	private Button btn_update;
	@FXML	private TextField tf_site;
	@FXML	private TextField tf_username;
	@FXML	private TextField tf_pass;
	@FXML	private ChoiceBox<Integer> cb_rownum;
	@FXML	private RadioButton rb_add;
	@FXML	private ToggleGroup one;
	@FXML	private RadioButton rb_edit;
	@FXML	private RadioButton rb_delete;
	@FXML	private TextArea ta_console;
	@FXML	private TableView<Password> tv_server;

	private Client alice;
	private Server bob;

	@SuppressWarnings("deprecation")
	@FXML
	void initialize() throws IOException {
		alice = new Client();
		bob = new Server();
		alice.sendPublicKeysToServer(bob);
		bob.sendPublicKeysToClient(alice);

		tv_server.setMaxWidth(Double.MAX_VALUE);
		tv_server.setMaxHeight(Double.MAX_VALUE);

		final TableColumn<Password, String> col1 = new TableColumn<>("Site");
		col1.setCellValueFactory(new PropertyValueFactory<>("site"));
		col1.impl_setWidth(100);

		final TableColumn<Password, String> col2 = new TableColumn<>("Username");
		col2.setCellValueFactory(new PropertyValueFactory<>("username"));
		col2.impl_setWidth(135);

		final TableColumn<Password, String> col3 = new TableColumn<>("Password");
		col3.setCellValueFactory(new PropertyValueFactory<>("password"));
		col3.impl_setWidth(135);

		tv_server.getColumns().add(col1);
		tv_server.getColumns().add(col2);
		tv_server.getColumns().add(col3);

		updateTableView();
	}

	void updateTableView() {
		ta_console.appendText("send to server: get passwords \n");
		alice.sendMessageToServer(bob, "get passwords");
		tv_server.getItems().clear();
		tv_server.setItems(FXCollections.observableArrayList(alice.getPasswords()));
		updateComboBox();
		tf_site.clear();
		tf_pass.clear();
		tf_username.clear();
	}

	void updateComboBox() {
		cb_rownum.getItems().clear();
		for (int i = 0; i < alice.getPasswords().size(); i++) {
			cb_rownum.getItems().add(i);
		}
		cb_rownum.getSelectionModel().select(0);
	}

	@FXML
	void comboOk(ActionEvent event) {
		Integer selectRow = new Integer(0);
		selectRow = cb_rownum.getSelectionModel().getSelectedItem();
		if (selectRow == null) {
			return;
		}
		Password row = tv_server.getItems().get(selectRow);
		tf_site.setText(row.getSite());
		tf_pass.setText(row.getPassword());
		tf_username.setText(row.getUsername());
	}

	@FXML
	void updateRequest(ActionEvent event) {
		SingleSelectionModel<Integer> temp = cb_rownum.getSelectionModel();
		if (temp == null)
			return;

		Integer temp1 = temp.getSelectedItem();
		if (temp1 == null)
			return;

		int selectRow = temp1.intValue();
		String site = tf_site.getText();
		String userName = tf_username.getText();
		String pass = tf_pass.getText();

		if (rb_add.isSelected()) {
			if (checkTextFieldsEmpty()) {
				ta_console.appendText("Error please enter values in text fields!\n");
				return;
			}
			Password newRow = new Password(site, userName, pass);
			ta_console.appendText("send to server: add " + newRow.toString() + "\n");
			alice.sendMessageToServer(bob, "add " + newRow.toString());
		}

		else if (rb_edit.isSelected()) {
			if (checkTextFieldsEmpty()) {
				ta_console.appendText("Error please enter values in text fields!\n");
				return;
			}
			List<Password> list = alice.getPasswords();
			list.get(selectRow).setSite(site);
			list.get(selectRow).setUsername(userName);
			list.get(selectRow).setPassword(pass);
			ta_console.appendText("send to server: edit " + alice.getPasswords().toString() + "\n");
			alice.sendMessageToServer(bob, "edit " + alice.getPasswords().toString());
		}

		else if (rb_delete.isSelected()) {
			alice.getPasswords().remove(selectRow);
			ta_console.appendText("send to server: delete " + selectRow + "\n");
			alice.sendMessageToServer(bob, "delete " + selectRow);
		}

		updateTableView();
	}

	boolean checkTextFieldsEmpty() {
		if (tf_site.getText().isEmpty() || tf_username.getText().isEmpty() || tf_pass.getText().isEmpty())
			return true;
		return false;
	}
}
