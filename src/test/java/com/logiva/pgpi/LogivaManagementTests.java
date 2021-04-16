package com.logiva.pgpi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class LogivaManagementTests {
	
	@Autowired
    PedidoRepository pedidoRespository;
	@Autowired
    ProductRepository productoRespository;
	@Autowired
    UsuarioRepository usuarioRespository;
    @Autowired
    PosicionRepository posicionRespository;
    @Autowired
    InstanciaProductoRepository instanciaProductoRespository;
    
    ProductController controller;
    
    @Test
    public void NotEmpty() {
    	Producto prod = new Producto(10, "test3", "Vacaco", 10);
    	productoRespository.save(prod);
    	//Producto prod2 = productoRespository.findByNombre("test3");
    	//assertEquals(prod.getProveedor(), prod2.getProveedor());
    }
    
    @Test
    public void Empty2() {
    	Producto prod2 = new Producto(5, "test4", "Vacaco", 10);
    	Producto prod = new Producto(2, "test3", "Vacaco", 10);
    	productoRespository.save(prod2);
    	productoRespository.save(prod);
    	//List<Producto> productos = productoRespository.findByCantidad(2);
    	//assertEquals(productos.get(0).getNombre(), "test3");
    }
    
    @Test
    public void Empty() {
    	List<Producto> productos = productoRespository.findAll();
    	assertEquals(productos.size(), 0);
    }
    
    @Test
    public void Product_added_succesfully() {
        // given
    	Producto prod = new Producto(10, "test3", "Vacaco", 10);
    	Producto prod2 = new Producto();
    	//List<Producto_cantidades> prod3 = controller.addProduct(prod);

        // when
        List<Producto> productos = productoRespository.findAll();
        for (Producto p: productos) {
        	if(p.getNombre().equals(prod.getNombre())) {
        		prod2=p;
        	}
        }
        System.out.print(prod2.getId());
        assertEquals(prod.getNombre(),prod2.getNombre());

    }

}
