package com.logiva.pgpi;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PgpiApplicationTests {
	
	@Autowired
    PedidoRepository pedidoRespository;
	@Autowired
    ProductRepository productoRespository;
    @Autowired
    PosicionRepository posicionRespository;
    @Autowired
    InstanciaProductoRepository instanciaProductoRespository; 
    
    @Autowired
    ProductController controller;
    
    String productname_1 = "test1";
    String productname_2 = "test2";
    String company = "Company";
    int quantity = 20;
    Date date = new Date();
    
    
    @BeforeAll
    public void init() {    	
    	controller.deleteAll();
    	controller.addProduct(new Producto(quantity, productname_2, company, 10));
    	Producto prod = productoRespository.findByNombre(productname_2);
    	controller.process_order(new Pedido(String.valueOf(prod.getId()),"20", "Direccion", "PREPARACION", 15, "Nombre", "Urgente", "DHL", date, date, (float) 2.4, 28023));
    	controller.init();
    }
    
    //Productos
    
    @Test
	public void test_list_products() {
		List<Producto_cantidades> productos = controller.producto_index();
		assertEquals(productos.size(), 1);
	}
	
    @Test
    public void test_add_product() {
    	Producto prod = new Producto(quantity, productname_1, company, 10);
    	controller.addProduct(prod);
    	Producto prod2 = productoRespository.findByNombre(productname_1);
    	assertEquals(prod.getNombre(), prod2.getNombre());
    }
    
	@Test
	public void test_list_product_byId() {
		Producto prod = productoRespository.findByNombre(productname_2);
		controller.addProductID(prod.getId());
		Producto prod2 = productoRespository.findByNombre(productname_2);
		assertEquals(prod.getCantidad() + 20, prod2.getCantidad());
	}
	
	@Test
	public void test_list_positions_product_byId() {
		Producto prod = productoRespository.findByNombre(productname_2);
		List<Posicion> posiciones = controller.posiciones_index_productId(String.valueOf(prod.getId()));
		
		assertEquals(posiciones.size(), 20);
	}	
	
	@Test
	public void test_productsByString() {
		List<Producto> productos = productoRespository.findProductsByIdString("1,2");
		assertEquals(productos.size(), 2);
	}
	
	//Pedidos
	
    @Test
	public void test_change_order_state() {
    	Producto prod = productoRespository.findByNombre(productname_2);
    	Pedido pedido = pedidoRespository.save(new Pedido(String.valueOf(prod.getId()),"5", "Direccion", "EN CAMINO", 15, "Nombre", "Urgente", "DHL", date, date, (float) 2.0, 28022));

    	Pedido pedido2 = controller.change_estado_recibido(String.valueOf(pedido.getId()));
    	
    	assertEquals("RECIBIDO", pedido2.getEstado());
	}
    
    @Test
	public void test_change_order_state_no() {
    	Producto prod = productoRespository.findByNombre(productname_2);
    	Pedido pedido = pedidoRespository.save(new Pedido(String.valueOf(prod.getId()),"1", "Direccion", "PREPARACION", 15, "Nombre", "Urgente", "DHL", date, date, (float) 2.0, 28022));

    	Pedido pedido2 = controller.change_estado_recibido(String.valueOf(pedido.getId()));
    	
    	assertEquals(null, pedido2);
	}
    
    @Test
	public void test_insert_order() {
   		List<Pedido> pedidos = controller.pedido_index();
		assertEquals(pedidos.size(), 3);
	}
    
    @Test
	public void test_pedido_pos() {
   		List<Pedido> pedidos = controller.pedido_index();
   		String id = String.valueOf(pedidos.get(0).getId());
   		List<Object> list = controller.pedido_pos(id);
		assertEquals(list.size(), 2);
	}
    
    @Test
   	public void test_del_ins_order() {
    	List<Posicion> posiciones = controller.posiciones_index();
    	int count = posiciones.size();
    	List<Pedido> pedidos = controller.pedido_index();
    	Pedido pedido = pedidos.get(0);
    	controller.delete_ins_pos_order(String.valueOf(pedido.getId()));
    	
    	List<Posicion> posiciones_2 = controller.posiciones_index();
    	int count_2 = posiciones_2.size();
    	
    	assertEquals(count - 20, count_2);
    }
    
	
	//Posiciones
	
    @Test
	public void test_list_positions() {
		List<Posicion> positions = controller.posiciones_index();
		assertEquals(positions.size(), 20);
	}
    
    @Test
	public void test_positions_instances() {
		List<Posicion> positions = posicionRespository.findAll();
		List<Instancia_Producto> instances = instanciaProductoRespository.findAll();
		for (int i=0; i<positions.size(); i++) {
			assertEquals(positions.get(i).getId(), instances.get(i).getIdposicion());
		}
	}
	
	//Instancias
	
    @Test
	public void test_list_instances() {
		List<Instancia_Producto> instances = controller.instancias_index();
		assertEquals(40, instances.size());
	}
    
}
