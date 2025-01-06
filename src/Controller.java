import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Controller {
    private VBox root;
    private TextField processIdField;
    private TextField arrivalTimeField;
    private TextField burstTimeField;
    private TextField priorityField;
    private TextField quantumTimeField;
    private ListView<String> processListView;
    private TableView<Process> resultTable;
    private Label avgTurnaroundTimeLabel;
    private Label avgWaitingTimeLabel;

    private ObservableList<String> processList;
    private List<Process> processes;
    private String selectedAlgorithm;
    private int roundRobinQuantum = 0; // Store Round Robin quantum time
    private boolean quantumLocked = false; // Flag to lock quantum time after set

    public Controller() {
        root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Advanced CPU Scheduling Simulator");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #333;");

        processIdField = new TextField();
        arrivalTimeField = new TextField();
        burstTimeField = new TextField();
        priorityField = new TextField();
        quantumTimeField = new TextField();
        priorityField.setDisable(true); // Priority field disabled by default
        quantumTimeField.setDisable(true); // Quantum time field disabled by default

        ComboBox<String> algorithmComboBox = new ComboBox<>();
        algorithmComboBox.getItems().addAll("FCFS", "SJF", "Round Robin", "Priority");
        algorithmComboBox.setOnAction(e -> updateFields(algorithmComboBox.getValue()));

        HBox inputBox = new HBox(10,
                createInputField("Process ID:", processIdField),
                createInputField("Arrival Time:", arrivalTimeField),
                createInputField("Burst Time:", burstTimeField),
                createInputField("Priority:", priorityField),
                createInputField("Quantum Time (for RR):", quantumTimeField));
        inputBox.setAlignment(Pos.CENTER);

        Button addProcessButton = new Button("Add Process");
        addProcessButton.getStyleClass().add("action-button");
        addProcessButton.setOnAction(e -> addProcess());

        Button simulateButton = new Button("Simulate");
        simulateButton.getStyleClass().add("action-button");
        simulateButton.setOnAction(e -> simulate(algorithmComboBox.getValue()));

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("action-button");
        clearButton.setOnAction(e -> clearAll());

        HBox buttonBox = new HBox(10, addProcessButton, simulateButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);

        processListView = new ListView<>();
        processList = FXCollections.observableArrayList();
        processListView.setItems(processList);

        resultTable = new TableView<>();
        resultTable.setPrefWidth(600);

        TableColumn<Process, String> idColumn = new TableColumn<>("Process ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);

        TableColumn<Process, Integer> arrivalTimeColumn = new TableColumn<>("Arrival Time");
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        arrivalTimeColumn.setPrefWidth(100);

        TableColumn<Process, Integer> burstTimeColumn = new TableColumn<>("Burst Time");
        burstTimeColumn.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        burstTimeColumn.setPrefWidth(100);

        TableColumn<Process, Integer> completionTimeColumn = new TableColumn<>("Completed Time");
        completionTimeColumn.setCellValueFactory(new PropertyValueFactory<>("completionTime"));
        completionTimeColumn.setPrefWidth(100);

        TableColumn<Process, Integer> waitingTimeColumn = new TableColumn<>("Waiting Time");
        waitingTimeColumn.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        waitingTimeColumn.setPrefWidth(100);

        TableColumn<Process, Integer> turnaroundTimeColumn = new TableColumn<>("Turnaround Time");
        turnaroundTimeColumn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        turnaroundTimeColumn.setPrefWidth(100);

        resultTable.getColumns().addAll(idColumn, arrivalTimeColumn, burstTimeColumn, completionTimeColumn, waitingTimeColumn, turnaroundTimeColumn);

        avgTurnaroundTimeLabel = new Label();
        avgWaitingTimeLabel = new Label();

        root.getChildren().addAll(titleLabel, algorithmComboBox, inputBox, buttonBox,
                new Label("Processes:"), processListView, resultTable, avgTurnaroundTimeLabel, avgWaitingTimeLabel);

        processes = new ArrayList<>();
    }

    private HBox createInputField(String labelText, TextField textField) {
        Label label = new Label(labelText);
        HBox box = new HBox(5, label, textField);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private boolean quantumSet = false; // Flag to check if quantum time is set

    private void updateFields(String algorithm) {
        selectedAlgorithm = algorithm;
        priorityField.setDisable(!"Priority".equals(algorithm));

        // Enable quantum time field only when Round Robin is selected and not set
        boolean isRoundRobin = "Round Robin".equals(algorithm);
        quantumTimeField.setDisable(!(isRoundRobin && !quantumSet));

        // Clear quantum time field when switching away from Round Robin
        if (!isRoundRobin) {
            quantumTimeField.clear();
            quantumSet = false; // Reset quantum set flag
        }
    }

    public void addProcess() {
        try {
            int arrivalTime = Integer.parseInt(arrivalTimeField.getText());
            int burstTime = Integer.parseInt(burstTimeField.getText());
            int priority = priorityField.getText().isEmpty() ? 0 : Integer.parseInt(priorityField.getText());

            // Ensure arrivalTime and burstTime are non-negative
            if (arrivalTime < 0 || burstTime <= 0 || priority < 0) {
                return;
            }

            // Store process with the input quantum time for Round Robin
            if ("Round Robin".equals(selectedAlgorithm) && !quantumSet) {
                roundRobinQuantum = Integer.parseInt(quantumTimeField.getText());
                quantumSet = true; // Set quantum set flag
            }

            Process process = new Process(processIdField.getText(), arrivalTime, burstTime, priority);
            processes.add(process);
            processList.add(String.format("%s: AT = %d, BT = %d, P = %d",
                    process.getId(), arrivalTime, burstTime, priority));

            clearInputs();
        } catch (NumberFormatException e) {
            // Handle the case where parsing fails
            System.err.println("Error parsing input: " + e.getMessage());
        }
    }

    private void clearInputs() {
        processIdField.clear();
        arrivalTimeField.clear();
        burstTimeField.clear();
        priorityField.clear();
        // Do not clear quantumTimeField if Round Robin is selected and quantum is already set
        if (!("Round Robin".equals(selectedAlgorithm) && quantumSet)) {
            quantumTimeField.clear();
        }
    }

    private void clearAll() {
        processList.clear();
        processes.clear();
        resultTable.getItems().clear();
        avgTurnaroundTimeLabel.setText("");
        avgWaitingTimeLabel.setText("");

        roundRobinQuantum = 0; // Reset quantum time on clear
        quantumSet = false; // Reset quantum set flag on clear
        quantumTimeField.clear(); // Clear the quantum time field
    }

    public void simulate(String algorithm) {
        switch (algorithm) {
            case "FCFS":
                simulateFCFS();
                break;
            case "SJF":
                simulateSJF();
                break;
            case "Round Robin":
                simulateRR();
                break;
            case "Priority":
                simulatePriority();
                break;
        }
    }

    public void simulateFCFS() {
        processes.sort((a, b) -> a.getArrivalTime() - b.getArrivalTime());
        simulate("FCFS", processes, 0);
    }

    public void simulateSJF() {
        processes.sort((a, b) -> a.getBurstTime() - b.getBurstTime());
        simulate("SJF", processes, 0);
    }

    public void simulateRR() {
        if (roundRobinQuantum == 0) {
            // Handle scenario where quantum time is note set
            return;
        }

        simulate("Round Robin", processes, roundRobinQuantum);
    }

    public void simulatePriority() {
        processes.sort((a, b) -> a.getPriority() - b.getPriority());
        simulate("Priority", processes, 0);
    }

    private void simulate(String algorithm, List<Process> processes, int quantum) {
        List<Process> resultProcesses = new ArrayList<>();
        int currentTime = 0;

        switch (algorithm) {
            case "FCFS":
            case "SJF":
            case "Priority":
                for (Process process : processes) {
                    int startTime = Math.max(currentTime, process.getArrivalTime());
                    int endTime = startTime + process.getBurstTime();
                    process.setCompletionTime(endTime);
                    process.setTurnaroundTime(endTime - process.getArrivalTime());
                    process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
                    resultProcesses.add(process);
                    currentTime = endTime;
                }
                break;

            case "Round Robin":
                Queue<Process> queue = new LinkedList<>(processes);
                int[] remainingBurstTimes = new int[processes.size()];
                for (int i = 0; i < processes.size(); i++) {
                    remainingBurstTimes[i] = processes.get(i).getBurstTime();
                }
                while (!queue.isEmpty()) {
                    Process process = queue.poll();
                    int processIndex = processes.indexOf(process);
                    int executeTime = Math.min(quantum, remainingBurstTimes[processIndex]);
                    remainingBurstTimes[processIndex] -= executeTime;
                    int startTime = Math.max(currentTime, process.getArrivalTime());
                    int endTime = startTime + executeTime;
                    currentTime = endTime;
                    if (remainingBurstTimes[processIndex] > 0) {
                        queue.offer(process);
                    } else {
                        process.setCompletionTime(currentTime);
                        process.setTurnaroundTime(currentTime - process.getArrivalTime());
                        process.setWaitingTime(process.getTurnaroundTime() - process.getBurstTime());
                        resultProcesses.add(process);
                    }
                }
                break;
        }

        ObservableList<Process> resultData = FXCollections.observableArrayList(resultProcesses);
        resultTable.setItems(resultData);

        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        for (Process p : resultProcesses) {
            totalWaitingTime += p.getWaitingTime();
            totalTurnaroundTime += p.getTurnaroundTime();
        }
        double avgWaitingTime = totalWaitingTime / resultProcesses.size();
        double avgTurnaroundTime = totalTurnaroundTime / resultProcesses.size();
        avgWaitingTimeLabel.setText(String.format("Average Waiting Time: %.2f", avgWaitingTime));
        avgTurnaroundTimeLabel.setText(String.format("Average Turnaround Time: %.2f", avgTurnaroundTime));
    }

    public VBox getRoot() {
        return root;
    }
}

