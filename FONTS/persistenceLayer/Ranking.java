package persistenceLayer;

public class Ranking {
	//clase auxiliar para el ranking, supone una entrada del Object[][]
	public String username;
	public int total, hard, medium, easy;
	
	//unica funcion creadora: asigna los diferentes valores 
	public Ranking(String user, Integer[] values) {
		this.username = user;
		this.total = values[0];
		this.hard = values[1];
		this.medium = values[2];
		this.easy = values[3];
	}
}
