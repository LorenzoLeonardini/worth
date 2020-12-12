package dev.leonardini.worth.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import dev.leonardini.worth.client.ClientAPI;

public class ProjectListPanel extends JPanel {
	
	private static final long serialVersionUID = -6191694052010996354L;

	private ClientAPI clientApi;
	private SpringLayout layout;
	private JLabel noProjects;
	private Map<String, JLabel> projects = new HashMap<String, JLabel>();
	private MainPanel mainPanel;
	
	public ProjectListPanel(MainPanel mainPanel, ClientAPI clientApi) {
		this.mainPanel = mainPanel;
		this.clientApi = clientApi;
		
		layout = new SpringLayout();
		setLayout(layout);

		JButton refresh = new JButton("Aggiorna");
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		refresh.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, refresh, 6, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, refresh, -6, SpringLayout.EAST, this);
		add(refresh);
		
		ProjectListPanel _this = this;
		JButton newProject = new JButton("Nuovo");
		newProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog f = new CreateProjectScreen(clientApi, _this);
				f.setVisible(true);
			}
		});
		newProject.setPreferredSize(new Dimension(130, 30));
		layout.putConstraint(SpringLayout.NORTH, newProject, 0, SpringLayout.NORTH, refresh);
		layout.putConstraint(SpringLayout.WEST, newProject, 6, SpringLayout.WEST, this);
		add(newProject);
		
		noProjects = new JLabel("Nessun progetto");
		noProjects.setVerticalAlignment(SwingConstants.CENTER);
		noProjects.setHorizontalAlignment(SwingConstants.CENTER);
		layout.putConstraint(SpringLayout.NORTH, noProjects, 6, SpringLayout.SOUTH, refresh);
		layout.putConstraint(SpringLayout.WEST, noProjects, 6, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, noProjects, -6, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, noProjects, -6, SpringLayout.SOUTH, this);
		add(noProjects);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateUI();
			}
		});
		
		refresh();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize();
			}
		});
	}
	
	public void refresh() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		new Thread(() -> {
			List<String> prs = clientApi.listProjects();
			for(String p : prs) {
				if(!projects.containsKey(p)) {
					addProject(p);
				}
			}
			if(projects.size() > 0)
				noProjects.setVisible(false);
			else
				noProjects.setVisible(true);
			setCursor(Cursor.getDefaultCursor());
			updateSize();
			updateUI();
		}).start();
	}
	
	private void updateSize() {
		if(getParent() != null && getParent().getParent() != null) {
			Dimension parentSize = getParent().getParent().getPreferredSize();
			setPreferredSize(new Dimension(parentSize.width, Math.max(parentSize.height, (projects.size() / 2) * 162 + 50)));
		}
	}
	
	private void addProject(String projectName) {
		JLabel project = new JLabel(projectName);
		project.setVerticalAlignment(SwingConstants.CENTER);
		project.setHorizontalAlignment(SwingConstants.CENTER);
		project.setBorder(BorderFactory.createLineBorder(Color.black));
		project.setBackground(FontUtils.RANDOM_COLOR());
		project.setOpaque(true);
		project.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		project.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mainPanel.openProject(projectName);
			}
		});
		
		layout.putConstraint(SpringLayout.NORTH, project, ((int)(projects.size() / 2)) * (150 + 12), SpringLayout.NORTH, noProjects);
		layout.putConstraint(SpringLayout.WEST, project, 6 + ((projects.size() + 1) % 2) * 6, projects.size() % 2 == 0 ? SpringLayout.WEST : SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.EAST, project, -6 + (projects.size()) % 2 * -6, projects.size() % 2 == 0 ? SpringLayout.HORIZONTAL_CENTER : SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.SOUTH, project, 150, SpringLayout.NORTH, project);
		add(project);
		projects.put(projectName, project);
	}

}
