/**
 * Implementación de árbol de decisiones ID3
 */
package weka.classifiers.trees;

import java.util.ArrayList;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Attribute;

/**
 * @author Diego
 */
public class ID3 extends AbstractClassifier {

	/**
	 * variables globales
	 */
	double clase;
	Nodo raiz;

	private static final long serialVersionUID = 1789045553777829235L;


	/**
	 * @param datos	Toma un conjunto de instancias y construye el árbol
	 */
	@Override
	public void buildClassifier(Instances datos) throws Exception {

		arbol(datos);

		// Comparar los demás atributos con el siguiente nodo
		for(int i = 0; i < raiz.atributo.numValues(); i ++){
			Instances datosNuevos = new Instances(datos);
			for(int j = 0; j < datosNuevos.size(); j ++){
				if(datosNuevos.get(j).value(raiz.atributo) != i){
					datosNuevos.delete(j);
				}
			}
			System.out.println(datosNuevos);
		}

		clase = datos.get(0).value(datos.get(0).classAttribute());
	}

	private void arbol(Instances datos) {
		// Mayor ganancia
		double mayorGanancia = Double.NEGATIVE_INFINITY;

		// Número total de atributos
		int totalAtributos = datos.numAttributes();

		// Establece la clase: play {no : 0, yes : 1}
		datos.setClassIndex(datos.numAttributes() - 1);

		// Inicializa arreglo de "double" con el tamaño de los atributos
		double[] contadorClase = new double[datos.attribute(totalAtributos - 1).numValues()]; 

		// Inicializa arreglo de nodos con los nombres de los atributos
		ArrayList<Nodo> nodos = new ArrayList<Nodo>();

		// Cálculo de la entropía de la clase
		for(Instance dato : datos){
			contadorClase[(int) dato.classValue()] ++;
		}

		double entropiaClase = calcularEntropia(contadorClase);

		// Cálculo de ganancias y eleccción del nodo con mayor ganancia
		for(int i = 0; i < totalAtributos - 1; i++){
			Nodo nodo = new Nodo(datos.attribute(i), calcularGanancia(datos, datos.attribute(i), entropiaClase));
			nodos.add(nodo);

			// Busca el nodo con mayor ganancia
			if(nodo.ganancia > mayorGanancia){
				raiz = nodo;
				mayorGanancia = nodo.ganancia;
			}
		}
	}

	/**
	 * Toma una instancia de ejemplo y la clasifica con base al árbol
	 * @param ejemplo Instancia de ejemplo
	 */
	@Override
	public double classifyInstance(Instance ejemplo) throws Exception {
		String valor = raiz.Evalua(ejemplo);
		return clase;
	}

	/**
	 * Calcula la entropía de los valores
	 * @param valores Arreglo de dobles que contiene el número de valores
	 */
	public double calcularEntropia(double[] valores) {
		// E = -p(+) log_2 (p(+)) - p(-) log_2 (p(-))
		double entropia = 0.0;
		double p = 0.0;
		double total = 0.0;
		
		// Obtiene el número total de casos
		for(double valor : valores){
			total += valor;
		}

		// Calcula cada término de la entropía en el arreglo
		for(double valor: valores){
			// p (probabilidad) = (cantidad positivos o negativos)/total
			p = (double) valor / total;

			// Evitar logaritmos negativos
			if(p == 0){
				return entropia = 0.0;
			}
			entropia -= p * (Math.log(p) / Math.log(2));
		}

		return entropia;
	}
	

	/**
	 * Calcula la entropía de las ramas de un atributo y la ganancia
	 * @param datos			Instancias
	 * @param atributo		Atributo en cuestión a evaluar sus ramas
	 * @param entropiaTotal	Entropía de todas las instancias
	 * @return 				Ganancia
	 */
	public double calcularGanancia(Instances datos, Attribute atributo, double entropiaTotal){
		double ganancia;
		double[] valores;
		double entropias = 0.0;

		// Itera los valores del atributo
		for(int i = 0; i < atributo.numValues(); i ++){

			// En cada iteración reinicia el arreglo de valores de clase
			valores = new double[2];

			// Itera las instancias
			for(Instance dato : datos){

				// Determina si el valor del atributo en esa instancia es igual al valor actual
				if(dato.stringValue(atributo).equals(atributo.value(i))){

					// Aumenta contador
					valores[(int) dato.classValue()] ++;
				}
			}

			// Aumenta el término negativo de la ecuación
			// E_rama = E_t * p(total) 
			entropias -= calcularEntropia(valores) * (valores[0] + valores[1]) / datos.numInstances();
		}

		// G(S, Atributo) = E_S - suma(E_ramas)
		return ganancia = entropiaTotal + entropias;
	}

	/**
	 * Imprime el árbol en weka
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return raiz.toString();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Para pruebas
		ID3 id3 = new ID3();

		// Lee datos de tenis
		DataSource source = new DataSource("weather.nominal.arff");
		Instances datos = source.getDataSet();

		id3.buildClassifier(datos);
	}

}
