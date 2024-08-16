# Implementación de ID3 en Java para Weka


Este algoritmo consiste de 4 métodos adicionales a los que solicita weka, los cuales son:

1.
``` java
calcularEntropia()
```
2.
``` java
calcularGanancia()
```

3.
``` java
encontrarMejor()
```

4.
``` java
arbol()
```

El primer método recibe un arreglo de "double" para calcular la entropía de los valores de la clase de un atributo. Este método es utilizado en el segundo método, ya que este realiza una diferencia
de las entropías con respecto a una entropía previamente calculada que corresponde a una entropía general. El cálculo de esta ganancia es útil para encontrar el mejor atributo de un conjunto de 
instancias, por lo que el método es utilizado en "encontrarMejor()" el cual retorna un atributo que corresponde al atributo con mayor ganancia y menor entropía, este último método es utilizado para
construir el árbol con el método "arbol()", este método itera en las ramas del mejor atributo encontrado con base a la ganancia, para iterar dichas ramas, se realiza una nueva instancia de los datos para
filtrar únicamente los valores que contengan el valor de la rama en cuestión, para este caso inicialicé un dato de tipo Instances con los datos y después los eliminé para quedar con una variable vacía, ya 
que de otra forma, refiriéndome a hacer una copia de los datos, tenía problemas, ya que al eliminar los atributos y tratar de filtrarlos, modificaba el conjunto de datos y sus posiciones, por lo que cuando
después trataba de identificar la instancia que deseaba eliminar con la posición, esta ya había cambiado y me eliminaba un valor diferente al que yo deseaba, fue por ello que opté por hacer una variable
vacía y agregarle los datos con forme los vaya encontrando, esto da como resultado un conjunto de instancias los datos que contienen el valor de la rama. Teniendo este conjunto de instancias pasé a hacer
un conteo  de el número de valores para cada clase, ya que si se trataba de un conjunto de instancias cuyos valores eran totalmente "yes" o "no", estamos hablando de una hoja y la iteración debe ser 
detenida, es por ello que realicé otra sentencia "if" que valida el valor de dicho conteo, el cual, si uno de los dos coincide con el tamaño del conjunto de datos, significará que debemos agregar un hijo a la
raíz cuyo valor sea "yes" o "no", dependiendo el caso, y  ya no se deberá llamar recursivamente a la función "arbol()", de lo contrario, se elimina el atributo que estamos analizando y llamamos de forma
recursiva al método "arbol()" y el resultado de este método, el cual será un sub-árbol, lo agregamos al nodo raíz. Por último, este método regresa nodo raíz en caso de que el mejor atributo no sea null, de
lo contrario, regresa un nodo vacío.
Por último, en el método "classifyInstance()", evalúa la instancia que se desea agregar y regresa el valor equivalente, es decir, 0 si es "no" y 1 si es "yes".
