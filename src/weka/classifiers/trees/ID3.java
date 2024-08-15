/**
 * Implementación de árbol de decisiones ID3
 */
package weka.classifiers.trees;

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

		raiz = arbol(datos);

		clase = datos.get(0).value(datos.get(0).classAttribute());
	}

	/**
	 * Método que construye el árbol
	 * @param datos Instancias
	 * @return Nodo raiz del árbol
	 */
	public Nodo arbol(Instances datos){

		// Recupera el mejor atributo
		Attribute mejorAtributo = encontrarMejor(datos);
		if(mejorAtributo != null){
			Nodo mejor = new Nodo(mejorAtributo);

			int index = 0;

			// Encuentra el índice del mejor atributo
			for(int i = 0; i < datos.numAttributes(); i ++){
				if(datos.attribute(i).equals(mejorAtributo)){
					index = i;
					break;
				}
			}
			for(int i = 0; i < mejorAtributo.numValues(); i++){
				
				// Inicializar la variable que almacenará las instancias
				Instances nuevosDatos = new Instances(datos);
				nuevosDatos.delete();
		
				// Filtro únicamente las instancias con el valor de la rama
				for(Instance dato : datos){
					if(dato.value(mejorAtributo) == mejorAtributo.indexOfValue(mejorAtributo.value(i))){
						nuevosDatos.add(dato);
					}
				}
				int contadorYes = 0, contadorNo = 0;
				for(Instance dato : nuevosDatos){
					if(dato.classValue() == 1){
						contadorYes ++;
					} else {
						contadorNo ++;
					}
				}

				if(contadorYes == nuevosDatos.size()){
					mejor.AgregaHijo( new Nodo(datos.classAttribute().value(1)), mejorAtributo.value(i));
				} else if (contadorNo == nuevosDatos.size()) {
					mejor.AgregaHijo( new Nodo(datos.classAttribute().value(0)), mejorAtributo.value(i));
				} else {
					nuevosDatos.deleteAttributeAt(index);
					Nodo subArbol = arbol(nuevosDatos);
					mejor.AgregaHijo(subArbol, mejorAtributo.value(i));
				}
			}
			return mejor;
		}
		return new Nodo(BATCH_SIZE_DEFAULT);
	}

	/**
	 * Método que encuentra el atributo con mayor ganancia en un conjunto de datos
	 * @param datos Instancias
	 * @return
	 */
	private Attribute encontrarMejor(Instances datos) {
		// Mayor ganancia
		double mayorGanancia = Double.NEGATIVE_INFINITY;

		// Número total de atributos
		int totalAtributos = datos.numAttributes();

		// Establece la clase: play {no : 0, yes : 1}
		datos.setClassIndex(datos.numAttributes() - 1);

		// Inicializa arreglo de "double" con el tamaño de los atributos
		double[] contadorClase = new double[datos.attribute(totalAtributos - 1).numValues()]; 

		// Cálculo de la entropía de la clase
		for(Instance dato : datos){
			contadorClase[(int) dato.classValue()] ++;
		}

		double entropiaClase = calcularEntropia(contadorClase);

		// Inicializa la ganancia
		double ganancia = 0.0;

		// Inicializa el mejor atributo a seleccionar
		Attribute mejorAtributo = null;

		// Cálculo de ganancias y eleccción del atributo con mayor ganancia
		for(int i = 0; i < totalAtributos - 1; i++){

			// Calcula la ganancia 
			ganancia = calcularGanancia(datos, datos.attribute(i), entropiaClase);

			// Busca el nodo con mayor ganancia
			if(ganancia > mayorGanancia){
				mejorAtributo = datos.attribute(i);
				mayorGanancia = ganancia;
			}
		}
		return mejorAtributo;
	}

	/**
	 * Toma una instancia de ejemplo y la clasifica con base al árbol
	 * @param ejemplo Instancia de ejemplo
	 */
	@Override
	public double classifyInstance(Instance ejemplo) throws Exception {
		String valor = raiz.Evalua(ejemplo);
		clase = Double.parseDouble(valor);
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
		return entropiaTotal + entropias;
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
