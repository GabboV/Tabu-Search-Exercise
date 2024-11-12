import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Main {

    // Classe Job che rappresenta ogni job con p_j, d_j, w_j
    static class Job {
        int id;
        int weight;         // w_j
        int processingTime; // p_j
        int dueDate;        // d_j

        public Job(int id, int weight, int processingTime, int dueDate) {
            this.id = id;
            this.weight = weight;
            this.processingTime = processingTime;
            this.dueDate = dueDate;
        }
    }

    public static void main(String[] args) {
        // Definizione dei lavori
        Job[] jobs = {
            new Job(1, 1, 6, 9),
            new Job(2, 1, 4, 12),
            new Job(3, 1, 8, 15),
            new Job(4, 1, 2, 8),
            new Job(5, 1, 10, 20),
            new Job(6, 1, 3, 22)
        };

        // Ordina i job per Earliest Due Date
        Job[] eddSequence = sortJobsByEarliestDueDate(jobs);
        
        System.out.println("STARTING FEASIBLE SOLUTION, sorted by earliest due date:");
        printSequence(eddSequence);
        
        // Calcola la sequenza ottimale
        Job[] tsSequence = tabuSearch(eddSequence);
        
        System.out.println("\nBEST SOLUTION found with Tabu Search:");
        printSequence(tsSequence);
    }

	private static void printSequence(Job[] currentSequence) {
		// Stampa il risultato in formato tabellare
        System.out.println("Current sequence:");
        System.out.printf("%-10s %-15s %-15s %-15s\n", "Job ID", "Cj", "Dj", "Penalty");

        int currentTime = 0; // Variabile per il calcolo del completion time
        for (Job job : currentSequence) {
            currentTime += job.processingTime; // Calcolo del completion time
            int tardiness = Math.max(0, currentTime - job.dueDate); // [C_j - d_j]+
            System.out.printf("%-10d %-15d %-15d %-15d\n", job.id, currentTime, job.dueDate, tardiness);
        }
        // Calcola il ritardo totale ponderato
        int totalWeightedTardiness = calculateTotalWeightedTardiness(currentSequence);
        System.out.println("Objective function: " + totalWeightedTardiness);
	}
    
    /**
     * Funzione che ordina i job in base alla Earliest Due Date (EDD).
     *
     * @param jobs Array di job da ordinare.
     * @return Array di job ordinati per Earliest Due Date.
     */
    public static Job[] sortJobsByEarliestDueDate(Job[] jobs) {
        // Crea una copia dell'array originale per evitare modifiche
        Job[] sortedJobs = Arrays.copyOf(jobs, jobs.length);
        
        // Ordina per `dueDate` in ordine crescente
        Arrays.sort(sortedJobs, Comparator.comparingInt(job -> job.dueDate));
        
        return sortedJobs;
    }

    /**
     * Metodo per trovare la sequenza ottimale dei lavori utilizzando la Tabu Search.
     * 
     * @param jobs Array di lavori.
     * @return Array di lavori nella sequenza ottimale trovata.
     */
    public static Job[] tabuSearch(Job[] currentSolution) {
        Job[] bestSolution = null;
        int bestObjective = Integer.MAX_VALUE;

        // Lista Tabu per tenere traccia delle soluzioni proibite
        List<String> tabuList = new ArrayList<>();
        int tabuTenure = 10; // Definisce la durata della "proibizione" nella lista Tabu
        int maxIterations = 100;
        
        int counter = 0;
        for (int iter = 0; iter < maxIterations; iter++) {
        	counter++;
            int currentBestObjective = calculateTotalWeightedTardiness(currentSolution);
            int currentMoveBestObj = Integer.MAX_VALUE;
            int swapIndex1 = -1, swapIndex2 = -1;

            // Prova tutte le coppie di job per effettuare swap e trova il miglior miglioramento
            for (int i = 0; i < currentSolution.length - 1; i++) {
                for (int j = i + 1; j < currentSolution.length; j++) {
                    swap(currentSolution, i, j);

                    int objective = calculateTotalWeightedTardiness(currentSolution);
                    String swapCode = i + "," + j;

                    // Controlla se la soluzione corrente è migliore e non è Tabu
                    if (objective < currentMoveBestObj && !tabuList.contains(swapCode)) {
                        currentMoveBestObj = objective;
                        swapIndex1 = i;
                        swapIndex2 = j;
                    }

                    // Annulla lo swap per provare la prossima coppia
                    swap(currentSolution, i, j);
                }
            }

            // Esegui il miglioramento trovato
            swap(currentSolution, swapIndex1, swapIndex2);
            System.out.println("\nIteration " + counter);
            System.out.println("Swapped positions " + swapIndex1 + "," + swapIndex2);
            printSequence(currentSolution);
            currentBestObjective = calculateTotalWeightedTardiness(currentSolution);
            
            // Aggiorna la lista Tabu
            tabuList.add(swapIndex1 + "," + swapIndex2);
            if (tabuList.size() > tabuTenure) {
                tabuList.remove(0); // Rimuove la soluzione più vecchia dalla lista Tabu
            }
            System.out.println("Tabu List:");
            for (String tabuMove : tabuList) {
                System.out.println("Tabu Move: " + tabuMove);
            }
            System.out.println("Total moves in Tabu List: " + tabuList.size());
            
            if (currentBestObjective < bestObjective) {
            	bestObjective = currentBestObjective;
                bestSolution = currentSolution.clone();
            }
        }

        return bestSolution;
    }

    /**
     * Metodo per calcolare il ritardo totale ponderato.
     * 
     * @param jobs Array di lavori nella sequenza scelta.
     * @return Ritardo totale ponderato.
     */
    public static int calculateTotalWeightedTardiness(Job[] jobs) {
        int currentTime = 0; // Tempo attuale, aumenta con l'aggiunta dei processing time dei lavori
        int totalWeightedTardiness = 0;

        for (Job job : jobs) {
            // Calcola il tempo di completamento C_j per il lavoro corrente
            currentTime += job.processingTime;
            int tardiness = Math.max(0, currentTime - job.dueDate); // [C_j - d_j]+
            totalWeightedTardiness += job.weight * tardiness; // w_j * tardiness
        }

		return totalWeightedTardiness;
	}
    
    /**
     * Scambia due job nell'array.
     * 
     * @param jobs Array di lavori.
     * @param i Primo indice.
     * @param j Secondo indice.
     */
    public static void swap(Job[] jobs, int i, int j) {
        Job temp = jobs[i];
        jobs[i] = jobs[j];
        jobs[j] = temp;
    }
}
