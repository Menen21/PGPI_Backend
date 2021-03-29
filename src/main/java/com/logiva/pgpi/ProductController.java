package com.logiva.pgpi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProductController {

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


    //Listing Products
    @GetMapping("PGPI/api/backend/producto/index")
    public List<Producto_cantidades> producto_index(){
    	List<Producto> productos = productoRespository.findAll();
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	List<Producto_cantidades> productos_cantidades = new ArrayList<Producto_cantidades>();

    	for (Producto p: productos) {
    		int columna =  return_position_product(p.getId(), posiciones, instancias);
    		int stock = count_products(1, columna, posiciones);
    		int preparacion = count_products(2, columna, posiciones);
    		productos_cantidades.add(new Producto_cantidades(p, stock, preparacion));
    	}
    	
        return productos_cantidades;
    }

    //Listing Orders
    @GetMapping("PGPI/api/backend/pedido/index")
    public List<Pedido> pedido_index(){
        return pedidoRespository.findAll();
    }

    //Listing all positions
    @GetMapping("PGPI/api/backend/posiciones/index")
    public List<Posicion> posiciones_index(){
        return posicionRespository.findAll();
    }

    //Listing all positions for a specific productId
    @GetMapping("PGPI/api/backend/posiciones/indexProduct")
    public List<Posicion> posiciones_index_productId(@RequestParam String id){
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	int columna =  return_position_product(Integer.parseInt(id), posiciones, instancias);

    	List<Posicion> posicionesProducto = new ArrayList<Posicion>();
    	for (Posicion pos: posiciones) {
    		if(pos.getColumna() == columna) {
    			posicionesProducto.add(pos);
    		}
    	}
        return posicionesProducto;
    }

    //Listing all products instances
    @GetMapping("PGPI/api/backend/instancias/index")
    public List<Instancia_Producto> instancias_index(){
        return instanciaProductoRespository.findAll();
    }


	//Adding Products
    @PostMapping("PGPI/api/backend/producto/add")
    public List<Producto_cantidades> create(@RequestBody Producto producto){
    	if (producto.getCantidad() > 40) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Product maximum quantity is 40.");
		}
    	int amount = producto.getCantidad();
    	List<Producto> productos = productoRespository.findAll();
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	List<Producto_cantidades> productos_cantidades = new ArrayList<Producto_cantidades>();
    	
    	//Look through the table if the Product already exists.
    	for (Producto p: productos) {
    		//If it exists we just add the amount to existing stock if there is space
    		if (p.getNombre().equals(producto.getNombre())){
    			int columna =  return_position_product(p.getId(), posiciones, instancias);
    			int count = count_products(1, columna, posiciones);
    			if (count >= 20) {
    				throw new ResponseStatusException(HttpStatus.CONFLICT, "Product stock is already full.");
    			}
    			p.setCantidad(p.getCantidad() + producto.getCantidad());
    			p = productoRespository.save(p);
    			createInstancesProductos(amount, p.getId());
    			
    			posiciones = posicionRespository.findAll();
    			int stock = count_products(1, columna, posiciones);
        		int preparacion = count_products(2, columna, posiciones);
        		productos_cantidades.add(new Producto_cantidades(p, stock, preparacion));
    			return productos_cantidades;
    		}
    	}
    	//If the product is new, we add it.
    	producto = productoRespository.save(producto);
    	createInstancesProductos(amount, producto.getId());
    	
    	posiciones = posicionRespository.findAll();
    	instancias = instanciaProductoRespository.findAll();
    	int columna =  return_position_product(producto.getId(), posiciones, instancias);
    	int stock = count_products(1, columna, posiciones);
		int preparacion = count_products(2, columna, posiciones);
		productos_cantidades.add(new Producto_cantidades(producto, stock, preparacion));
        return productos_cantidades;
    }

    //Get instances and positions for Client Order. 
    @PostMapping("PGPI/api/backend/pedido/order_pos")
    public List<Object> get_ins_pos(@RequestBody Pedido pedido){
    	List<Producto> productos = productoRespository.findAll();

    	for (Producto p: productos) {
    		if ((p.getId() == pedido.getId_producto()) & (p.getCantidad() >= pedido.getCantidad())){
    			List<Object> new_instancias = getInstancesProducts(pedido.getId_producto(), pedido.getCantidad());

    			pedido.setEstado("Preparación");
    			pedidoRespository.save(pedido);
    			return new_instancias;
    		}
    	}
    	pedido.setEstado("Preparación");
		pedidoRespository.save(pedido);
    	throw new ResponseStatusException(HttpStatus.INSUFFICIENT_STORAGE, "There is no stock. Restocking item.");
    }
    
    //Delete instances and positions for Client Order. 
    @PostMapping("PGPI/api/backend/pedido/order_del")
    public void delete_ins_pos(@RequestParam String id){
    	int ped_id = Integer.parseInt(id);
    	List<Producto> productos = productoRespository.findAll();
    	List<Pedido> pedidos = pedidoRespository.findAll();
    	
    	for(Pedido pedido: pedidos) {
    		if(pedido.getId()== ped_id) {
		    	for (Producto p: productos) {
		    		if ((p.getId() == pedido.getId_producto()) & (p.getCantidad() >= pedido.getCantidad())){
		    			p.setCantidad(p.getCantidad() - pedido.getCantidad());
		    			p = productoRespository.save(p);
		    			updateInstancesProducts(pedido.getId_producto(), pedido.getCantidad());
		
		    			//We need restock of the product.
		    			if(p.getCantidad() < p.getCantidad_minima_restock()) {
		    				System.out.println("Se necesita restock");
		    			}
		    			pedido.setEstado("En Camino");
		    			pedidoRespository.save(pedido);
		    		}
		    	}
    		}
    	}
    }

	//Add user
    @PostMapping("PGPI/api/backend/user/add")
    public Usuario adduser(@RequestBody Usuario usuario) {
    	List<Usuario> data = usuarioRespository.findAll();

    	for (Usuario u: data) {
    		if (u.getEmail().equals(usuario.getEmail())){
    			return null;
    			}
    		}

		return usuarioRespository.save(usuario);
    }

	//Testing User
    @GetMapping("PGPI/api/backend/user/test")
    public boolean testUser(@RequestBody Usuario usuario){

    	List<Usuario> data = usuarioRespository.findAll();

    	//Look through the table if the Product already exists.
    	for (Usuario u: data) {
    		if ((u.getEmail().equals(usuario.getEmail())) & (u.getPassword().equals(usuario.getPassword()))){
    			return true;
    		}
    	}
        return false;
    }

    //Delete all tables
    @PostMapping("PGPI/api/backend/deleteAll")
    public void deleteAll(){
    	pedidoRespository.deleteAll();
    	productoRespository.deleteAll();
    	usuarioRespository.deleteAll();
    	posicionRespository.deleteAll();
    	instanciaProductoRespository.deleteAll();
    }


    //Used to create product instances, used when adding products
    public void createInstancesProductos(int cantidad, int id_producto) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	int columna =  return_position_product(id_producto, posiciones, instancias);
    	List<Instancia_Producto> new_instancias = new ArrayList<Instancia_Producto>();

    	if (columna == 0){
    		columna = return_first_available_column(posiciones);
    		for (int i=0; i < 20; i++) {
	    		Posicion p = new Posicion(2, columna, "Preparación");
	    		p = posicionRespository.save(p);
	    	    new_instancias.add(new Instancia_Producto(id_producto, p.getId()));
	    	    cantidad--;
    		}
    	}
    	if(cantidad>0) {
	    	for (int i=0; i < cantidad; i++) {
	    		Posicion p = new Posicion(1, columna, "Stock");
	    		p = posicionRespository.save(p);
	    	    new_instancias.add(new Instancia_Producto(id_producto, p.getId()));
	    	}
    	}

    	instanciaProductoRespository.saveAll(new_instancias);
    }
    
    private List<Object> getInstancesProducts(int id_producto, int cantidad) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	
    	List<Object> instancias_posiciones = new ArrayList<Object>();

    	for (Instancia_Producto ins: instancias) {
    		for (Posicion pos: posiciones) {
    			if((ins.getId_producto() == id_producto) & (cantidad > 0) & (pos.getId() == ins.getId_posicion())) {
    				List<Object> pareja_pos_ins = new ArrayList<Object>();
    				pareja_pos_ins.add(pos);
    				pareja_pos_ins.add(ins);   		
    				instancias_posiciones.add(pareja_pos_ins);
	    			cantidad--;
	    		}
    			
    		}	    	
    	}
		return instancias_posiciones;
	}



    //Returns the first available column
    public int return_first_available_column(List<Posicion> posiciones) {
    	List<Integer> used_columns = new ArrayList<Integer>();
    	for (Posicion pos: posiciones) {
    		used_columns.add(pos.getColumna());
    	}
    	for (int i=1; i<=50; i++) {
    		if (!used_columns.contains(i)) {
    			return i;
    		}
    	}

    	return 0;
    }

    //Returns the column for a set product
    public int return_position_product(int id_producto, List<Posicion> posiciones, List<Instancia_Producto> instancias) {
    	for (Posicion pos: posiciones) {
    		if(pos.getFila() == 2){
    			for (Instancia_Producto instan: instancias) {
    				if((id_producto == instan.getId_producto()) & (pos.getId() == instan.getId_posicion())) {
    					return pos.getColumna();
    				}
    			}
    		}
    	}
		return 0;
    }


    //Used when there is an order, removes the products sold and moves inventory from 'Stock' to 'Preparation' if needed
    private void updateInstancesProducts(int id_producto, int cantidad) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();

    	int columna =  return_position_product(id_producto, posiciones, instancias);
		while (cantidad > 0) {
			int sold = deleteInstances(2, columna, posiciones, instancias, cantidad);
			posiciones = posicionRespository.findAll();
			instancias = instanciaProductoRespository.findAll();
			int count_left_prep = count_products(2, columna, posiciones);

			if(count_left_prep == 0) {
				updateInstances(1, columna, posiciones);
			}
			cantidad -= sold;
		}

	}

    //Move products from Stock to Preparation
    private void updateInstances(int fila, int columna, List<Posicion> posiciones) {
    	List<Posicion> new_posiciones = new ArrayList<Posicion>();
    	for (Posicion pos: posiciones) {
    		if((pos.getFila()==fila) & (pos.getColumna()==columna)) {
    			pos.setFila(2);
    			pos.setTipo("Preparación");
    			new_posiciones.add(pos);
    		}
    	}
    	posicionRespository.saveAll(new_posiciones);
	}

    //Delete products sold
	public int deleteInstances(int fila, int columna, List<Posicion> posiciones, List<Instancia_Producto> instancias, int cantidad) {
    	int counter=0;
    	if(cantidad > 20) {
    		cantidad = 20;
    	}
	    for (Posicion pos: posiciones) {
	    	if((pos.getFila()==fila) & (pos.getColumna()==columna)) {
	    		for (Instancia_Producto instan: instancias) {
	    			if(pos.getId() == instan.getId_posicion()) {
			    		posicionRespository.deleteById(pos.getId());
			    		instanciaProductoRespository.deleteById(instan.getId());
			    		counter++;
			    		cantidad--;

			    		if (cantidad < 1) {
			    			return counter;
			    		}
	    			}
	    		}
	    	}
	    }
    	return counter;
    }

	//Count products in a specific row and column
	private int count_products(int fila, int columna, List<Posicion> posiciones) {
		int count = 0;
		for (Posicion pos: posiciones) {
			if((pos.getFila()==fila) & (pos.getColumna()==columna)) {
				count++;
			}
		}

		return count;
	}



}
