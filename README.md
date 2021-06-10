# CHAT CIFRADO

## 1.	¿Cómo hicieron el programa?

Se creó la clase DHServer, la cual se encarga de crear una conexión con un cliente para intercambiar mensajes, para esto primero se realiza el intercambio de llaves utilizando Diffie-Hellman. Una vez realizado este intercambio, se calculó la llave de 128 bits para encriptar y desencriptar los mensajes utilizando el algoritmo AES. También, se creó una clase DHClient, que se encarga de generar las llaves de los clientes para entablar una conexión con el servidor. También, de enviar y recibir los diferentes mensajes y de encriptar y desencriptar los mensajes para que no sean fácil de leer si son interceptados.


## 2.	Dificultades del proyecto.

a.	Desconocimiento del paquete de encriptación de Java.
b.	“Race condition”.
c.	Entendimiento del algoritmo Diffie-Hellman.

## 3.	Conclusiones.

Gracias a la elaboración del proyecto se logró reforzar los temas y conocimientos de encriptación obtenidos en el curso (Diffie-Hellman, AES, etc.) como también de tener la capacidad de aplicarlo en un ámbito practico. Además, se puede concluir que gracias al cifrado o la encriptación podemos tener una comunicación más segura debido a la confiabilidad que esta nos provee porque nos permite que los mensajes no se puedan leer tan fácilmente si algún atacante quiere perjudicarnos. 
