package com.logiva.pgpi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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

    List<Instancia_Producto> instancias_disp = new ArrayList<Instancia_Producto>();
    @PostConstruct
    public void init() {
    	List<Pedido> pedidos = pedidoRespository.findAll();
    	instancias_disp.addAll(instanciaProductoRespository.findAll());
    	
    	for(Pedido ped: pedidos) {
    		if(ped.getEstado().equals("PREPARACION")) {
        		List<Integer> id_vals = get_values_string(ped.getId_producto());
        		List<Integer> cant_vals = get_values_string(ped.getCantidad());
        		for (int i = 0; i < id_vals.size();i++) {
        			 update_ins_disp_prod(id_vals.get(i), cant_vals.get(i));
        		}
    		}
    	}
    }
    
    public void update_ins_disp_prod(int prod_id, int cantidad){
    	for (Instancia_Producto ins_prod: instancias_disp) {
    		if((ins_prod.getId_producto() == prod_id) & (cantidad > 0) & (ins_prod.getDisponible()==1)) {
    			ins_prod.setDisponible(0);
    			cantidad--;
    		}
    	}
    }

    //Listing Products
    @GetMapping("PGPI/api/backend/producto/index")
    public List<Producto_cantidades> producto_index(){
    	List<Producto> productos = productoRespository.findAll();
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> instancias = instanciaProductoRespository.findAll();
    	List<Producto_cantidades> productos_cantidades = new ArrayList<Producto_cantidades>();

    	for (Producto p: productos) {
    		int columna =  return_position_product(p.getId(), posiciones, instancias);
    		int stock = count_products(1, columna);
    		int preparacion = count_products(2, columna);
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
    public List<Producto_cantidades> addProduct(@RequestBody Producto producto){
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
    			int count = count_products(1, columna);
    			if ((count >= 20) || (producto.getCantidad() > 20)) {
    				throw new ResponseStatusException(HttpStatus.CONFLICT, "Full, no space available for this product.");
    			}
    			p.setCantidad(p.getCantidad() + producto.getCantidad());
    			p = productoRespository.save(p);
    			createInstancesProductos(amount, p.getId());
    			
    			int stock = count_products(1, columna);
        		int preparacion = count_products(2, columna);
        		productos_cantidades.add(new Producto_cantidades(p, stock, preparacion));
    			return productos_cantidades;
    		}
    	}
    	//If the product is new, we add it.
    	producto = productoRespository.save(producto);
    	createInstancesProductos(amount, producto.getId());
    	
    	instancias = instanciaProductoRespository.findAll();
    	int columna =  return_position_product(producto.getId(), posiciones, instancias);
    	int stock = count_products(1, columna);
		int preparacion = count_products(2, columna);
		productos_cantidades.add(new Producto_cantidades(producto, stock, preparacion));
        return productos_cantidades;
    }
    

    //Process client order and get instances and positions for products. 
    @PostMapping("PGPI/api/backend/pedido/order_pos")
    public List<Object> get_ins_pos_order(@RequestBody Pedido pedido){
    	List<Integer> id_vals = get_values_string(pedido.getId_producto());
    	List<Integer> cant_vals = get_values_string(pedido.getCantidad());
    	List<Object> all_pos_prod = new ArrayList<Object>();
    	
    	if(id_vals.size() != cant_vals.size()) {
    		throw new ResponseStatusException(HttpStatus.CONFLICT, "Size missmatch. Quantities and number of products must be the same size.");
    	}

    	for(int i = 0; i < id_vals.size(); i++) {
    		all_pos_prod.add(get_save_ins_pos_product(pedido, id_vals.get(i), cant_vals.get(i)));
    	}
    	
    	pedido.setEstado("Preparación");
    	Pedido ped = pedidoRespository.save(pedido);
    	all_pos_prod.add(ped);
		return all_pos_prod;
    }
    
    //Return Product positions for a specific order
    @GetMapping("PGPI/api/backend/pedido/pedidoid_pos")
    public List<Object> pedido_pos(@RequestParam String id){
    	int ped_id = Integer.parseInt(id);
    	List<Pedido> pedidos = pedidoRespository.findAll();
    	List<Object> posiciones_pedido = new ArrayList<Object>();
    	
    	for(Pedido pedido: pedidos) {
    		if(ped_id == pedido.getId()) {
    			
    			List<Integer> id_vals = get_values_string(pedido.getId_producto());
    	    	List<Integer> cant_vals = get_values_string(pedido.getCantidad());
    	    	
    	    	for(int i = 0; i < id_vals.size(); i++) {
    	    		List<Object> pos_ins = get_ins_pos_product(pedidos, pedido, id_vals.get(i), cant_vals.get(i));
    	    		posiciones_pedido.add(pos_ins.get(0));
    	    	}
    	    	posiciones_pedido.add(pedido);
    		}
    	}
    	
        return posiciones_pedido;
    }

    private List<Object> get_ins_pos_product(List<Pedido> pedidos, Pedido pedido, Integer prod_id, Integer cantidad) {
    	int cantidad_resv = 0;
    	
    	for (Pedido ped: pedidos) {
    		List<Integer> id_vals = get_values_string(ped.getId_producto());
    		if((ped.getId() < pedido.getId()) & (id_vals.contains(prod_id)) & (ped.getEstado().equals(pedido.getEstado()))) {
    			List<Integer> id_vals_2 = get_values_string(ped.getId_producto());
    			int index = id_vals_2.indexOf(prod_id);
    			List<Integer> cant_vals = get_values_string(ped.getCantidad());
    			cantidad_resv = cantidad_resv + cant_vals.get(index);
    		}
    	}
    	
    	return InstancesProducts(prod_id, cantidad, cantidad_resv);
	}
    
    
  //Delete instances and positions for Client Order. 
    @PostMapping("PGPI/api/backend/pedido/order_del")
    public boolean delete_ins_pos_order(@RequestParam String id){
    	int ped_id = Integer.parseInt(id);
    	List<Pedido> pedidos = pedidoRespository.findAll();
    	
    	for (Pedido pedido: pedidos) {
    		if(pedido.getId() == ped_id) {
    			List<Integer> id_vals = get_values_string(pedido.getId_producto());
    	    	List<Integer> cant_vals = get_values_string(pedido.getCantidad());
    	    	
    	    	for(int i = 0; i < id_vals.size(); i++) {
    	    		delete_ins_pos_product(pedidos, pedido, id_vals.get(i), cant_vals.get(i));
    	    	}
    	    	pedido.setEstado("En Camino");
    	    	pedidoRespository.save(pedido);
    	    	return true;
    		}

    	}
    	
    	return false;
    }
    
        
    public List<Object> get_save_ins_pos_product(Pedido pedido, int id, int cantidad){
    	List<Producto> productos = productoRespository.findAll();

    	for (Producto p: productos) {
    		if ((p.getId() == id) & (p.getCantidad() >= cantidad)){
    			p.setCantidad(p.getCantidad() - cantidad);
    			productoRespository.save(p);
    			return getInstancesProducts(id, cantidad);
    		}
    	}
    	pedido.setEstado("Pendiente");
		pedidoRespository.save(pedido);
    	throw new ResponseStatusException(HttpStatus.INSUFFICIENT_STORAGE, "There is no stock. Restocking item.");
    }
    
    
    private List<Object> getInstancesProducts(int id_producto, int cantidad) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Object> instancias_posiciones = new ArrayList<Object>();
	    	
    	for (Instancia_Producto ins: instancias_disp) {
    		if((ins.getId_producto() == id_producto) & (ins.getDisponible() == 1)) {
	    		for (Posicion pos: posiciones) {
	    			if((cantidad > 0) & (pos.getId() == ins.getId_posicion())) {
	    				List<Object> pareja_pos_ins = new ArrayList<Object>();
	    				pareja_pos_ins.add(pos);
	    				pareja_pos_ins.add(ins);   		
	    				instancias_posiciones.add(pareja_pos_ins);
		    			cantidad--;
		    			ins.setDisponible(0);
		    		}
	    			
	    		}
    		}
    	}
		return instancias_posiciones;
	}
    
    
    //removes the products sold and moves inventory from 'Stock' to 'Preparation' if needed
	@SuppressWarnings("unchecked")
	public boolean delete_ins_pos_product(List<Pedido> pedidos, Pedido pedido, int prod_id, int cantidad){
    	List<Posicion> posiciones = posicionRespository.findAll();
    	int cantidad_resv = 0;
    	int columna =  return_position_product(prod_id, posiciones, instancias_disp);
    	
    	for (Pedido ped: pedidos) {
    		List<Integer> id_vals = get_values_string(pedido.getId_producto());
    		if((ped.getId() < pedido.getId()) & (id_vals.contains(prod_id)) & (ped.getEstado().equals(pedido.getEstado()))) {
    			List<Integer> id_vals_2 = get_values_string(ped.getId_producto());
    			int index = id_vals_2.indexOf(prod_id);
    			List<Integer> cant_vals = get_values_string(ped.getCantidad());
    			cantidad_resv = cantidad_resv + cant_vals.get(index);
    		}
    	}
    	List<Object> ins_pos= InstancesProducts(prod_id, cantidad, cantidad_resv);
  
    	List <Posicion> pos = (List<Posicion>) ins_pos.get(0);
    	List <Instancia_Producto> ins = (List<Instancia_Producto>) ins_pos.get(1);
    	
    	posicionRespository.deleteInBatch(pos);
    	instanciaProductoRespository.deleteInBatch(ins);
    	
    	int count_left_prep = count_products(2, columna);
		if(count_left_prep == 0) {
			updateInstances(1, columna, posiciones);
		}
    	return true;
    }
 

    
    /*
    //Add restock and process pending order. 
    @PostMapping("PGPI/api/backend/pedido/pendingOrder")
    public List<Object> pendingOrder(@RequestParam String id_pedido, String id_producto, String cantidad){
    	int id_prod = Integer.parseInt(id_producto);
    	int id_ped = Integer.parseInt(id_pedido);
    	int cant = Integer.parseInt(cantidad);
    	List<Producto> productos = productoRespository.findAll();
    	List<Pedido> pedidos = pedidoRespository.findAll();
    	List<Object> product_orderInstances = new ArrayList<Object>();
    	
    	for (Producto p: productos) {
    		if(p.getId() == id_prod) {
    			List<Producto_cantidades> producto_cantidades = addProduct(new Producto(p.getNombre(), cant));
    			product_orderInstances.add(producto_cantidades);
    		}
    	}
    	
    	for (Pedido ped: pedidos) {
    		if(ped.getId() == id_ped) {
    			List<Object> pos_ins= get_ins_pos(ped);
    	    	product_orderInstances.add(pos_ins);
    		}
    	}
    	
    	return product_orderInstances;
    }
	*/ 
    
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
		    	if(cantidad>0) {	
    				Posicion p = new Posicion(2, columna, "Preparación");
		    		p = posicionRespository.save(p);
		    	    new_instancias.add(new Instancia_Producto(id_producto, p.getId()));
		    	    cantidad--;
		    	}
    		}
    	}
    	if(cantidad>0) {
	    	for (int i=0; i < cantidad; i++) {
	    		Posicion p = new Posicion(1, columna, "Stock");
	    		p = posicionRespository.save(p);
	    	    new_instancias.add(new Instancia_Producto(id_producto, p.getId()));
	    	}
    	}
    	instancias_disp.addAll(new_instancias);
    	instanciaProductoRespository.saveAll(new_instancias);
    }
    
    //Used when there is an order, to get positions and instances
	private List<Object> InstancesProducts(int id_producto, int cantidad, int cantidad_resv) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> del_instancias = new ArrayList<Instancia_Producto>();
    	List<Posicion> del_posiciones = new ArrayList<Posicion>();
    	List<Object> pos_ins = new ArrayList<Object>();
	    	
    	while((cantidad_resv + cantidad > 0)) {
	        for (Instancia_Producto ins: instancias_disp) {
	       		if((ins.getId_producto() == id_producto) & (ins.getDisponible() == 0)) {
	       			for (Posicion pos: posiciones) {
	       				if (pos.getId() == ins.getId_posicion()) {
	       					if((cantidad > 0) & (cantidad_resv < 1)) {
				   	    		del_instancias.add(ins);
				   	    		del_posiciones.add(pos);
				   	    		cantidad--;
	       					}
	       					cantidad_resv--;
			        	}
	       			}
	        	}
	        }
    	}
    	pos_ins.add(del_posiciones);
    	pos_ins.add(del_instancias);
    	return pos_ins;
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
	private int count_products(int fila, int columna) {
		List<Posicion> posiciones = posicionRespository.findAll();
		int count = 0;
		for (Posicion pos: posiciones) {
			if((pos.getFila()==fila) & (pos.getColumna()==columna)) {
				count++;
			}
		}
		return count;
	}

	public List<Integer> get_values_string(String values) {
		return Arrays.stream(values.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
	}

}
