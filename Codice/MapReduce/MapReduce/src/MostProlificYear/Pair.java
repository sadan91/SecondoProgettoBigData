package MostProlificYear;

/**
 * Questa classe modella una coppia di valori (String, Integer) rappresentanti
 * l'anno ed il numero di film di quell'anno
 *
 */
public class Pair {

	public String year;
	public Integer moviesNumber;

	public Pair(String year, Integer moviesNumber) {
		this.year = year;
		this.moviesNumber = moviesNumber;
	}
};