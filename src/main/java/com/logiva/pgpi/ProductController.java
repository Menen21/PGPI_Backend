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
    

    @PostMapping("PGPI/api/backend/producto/test")
    public void test(){
    	List<Integer> ids = new ArrayList<Integer>();
    	List<Instancia_Producto> ins_prod0= instanciaProductoRespository.findByIdproducto(257);
    	for(Instancia_Producto in: ins_prod0) {
    		ids.add(in.getIdposicion());
    	}
    	List<Posicion> posi0 = posicionRespository.findByIdIn(ids);
    	
    	for(Posicion pos:posi0) {
    		System.out.println(pos.getId());
    	}
    	
    	/*
    	List<Producto> prods5 = productoRespository.findByCantidadLessThanEqual(20);
    	for (Producto p: prods5) {
    		System.out.println(p.getNombre());
    	}
    	
    	//productoRespository.customMethod();
    	Producto prods = productoRespository.findById(256);
    	System.out.println(prods.getNombre());
    	
    	List<Producto> prods2 = productoRespository.findProductsByIdString("257,258");
    	for (Producto p: prods2) {
    		System.out.println(p.getNombre());
    	}
    	*/
    }
    
    
    

    List<Instancia_Producto> instancias_disp = new ArrayList<Instancia_Producto>();
    
    @PostConstruct
    public void init() {
    	List<Pedido> pedidos = pedidoRespository.findByEstado("PREPARACION");
    	instancias_disp.addAll(instanciaProductoRespository.findAll());
    	
    	for(Pedido ped: pedidos) {
        	List<Integer> id_vals = get_values_string(ped.getId_producto());
        	List<Integer> cant_vals = get_values_string(ped.getCantidad());
        	for (int i = 0; i < id_vals.size();i++) {
        			update_ins_disp_prod(id_vals.get(i), cant_vals.get(i));
        	}
    	}
    }
    
    public void update_ins_disp_prod(int prod_id, int cantidad){
    	for (Instancia_Producto ins_prod: instancias_disp) {
    		if((ins_prod.getIdproducto() == prod_id) & (cantidad > 0) & (ins_prod.getDisponible()==1)) {
    			ins_prod.setDisponible(0);
    			cantidad--;
    		}
    	}
    }
    
    
    //Listing Products
    @GetMapping("PGPI/api/backend/producto/index")
    public List<Producto_cantidades> producto_index(){
    	return productoRespository.findProductoCantidades();   
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
    
    //Listing all products instances
    @GetMapping("PGPI/api/backend/instancias/index")
    public List<Instancia_Producto> instancias_index(){
        return instanciaProductoRespository.findAll();
    }
  

    //Listing all positions for a specific productId
    @GetMapping("PGPI/api/backend/posiciones/indexProduct")
    public List<Posicion> posiciones_index_productId(@RequestParam String id){
    	int id_int = Integer.parseInt(id);
    	List<Integer> ids = new ArrayList<Integer>();
    	List<Instancia_Producto> ins_prod= instanciaProductoRespository.findByIdproducto(id_int);
    	for(Instancia_Producto in: ins_prod) {
    		ids.add(in.getIdposicion());
    	}
        return posicionRespository.findByIdIn(ids);
    }

    
   //Adding Products by id
    @PostMapping("PGPI/api/backend/producto/addID")
    public Producto_cantidades addProductID(@RequestParam int id){
    	Producto producto = productoRespository.findById(id);
    	List<Object> ins_prod = productoRespository.saveProducto(producto, 20);
    	for(int i=0; i<ins_prod.size()-1; i++) {
    		instancias_disp.add((Instancia_Producto) ins_prod.get(i));
    	}
    	return (Producto_cantidades) ins_prod.get(ins_prod.size()-1);
    }
    
    
	//Adding Products
    @PostMapping("PGPI/api/backend/producto/add")
    public Producto_cantidades addProduct(@RequestBody Producto producto){
    	List<Object> ins_prod = productoRespository.saveProducto(producto, producto.getCantidad());
    	for(int i=0; i<ins_prod.size()-1; i++) {
    		instancias_disp.add((Instancia_Producto) ins_prod.get(i));
    	}
    	return (Producto_cantidades) ins_prod.get(ins_prod.size()-1);
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
    	
    	List<Integer> productos = productoRespository.availableProducts(pedido);
    	
    	if (productos.size() > 0) {
    		pedido.setEstado("Pendiente");
        	Pedido ped = pedidoRespository.save(pedido);
        	all_pos_prod.add(productos);
        	all_pos_prod.add(ped);
        	return all_pos_prod;
    	}

    	for(int i = 0; i < id_vals.size(); i++) {
    		all_pos_prod.add(get_save_ins_pos_product(pedido, id_vals.get(i), cant_vals.get(i)));
    	}
    	
    	pedido.setEstado("PREPARACION");
    	Pedido ped = pedidoRespository.save(pedido);
    	all_pos_prod.add(ped);
		return all_pos_prod;
    }
    
    public List<Object> get_save_ins_pos_product(Pedido pedido, int id, int cantidad){
    	List<Producto> productos = productoRespository.findAll();

    	for (Producto p: productos) {
    		if ((p.getId() == id) & (p.getCantidad() >= cantidad)){
    			p.setCantidad(p.getCantidad() - cantidad);
    			productoRespository.save(p);
    		}
    	}
    	return getInstancesProducts(id, cantidad);
    }
    
    private List<Object> getInstancesProducts(int id_producto, int cantidad) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Object> instancias_posiciones = new ArrayList<Object>();
	    	
    	for (Instancia_Producto ins: instancias_disp) {
    		if((ins.getIdproducto() == id_producto) & (ins.getDisponible() == 1)) {
	    		for (Posicion pos: posiciones) {
	    			if((cantidad > 0) & (pos.getId() == ins.getIdposicion())) {
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
    
	//Set Order state from "En camino" to "Recibido"
	@PostMapping("PGPI/api/backend/pedido/order_state_recieved")
    public Pedido change_estado_recibido(@RequestParam String id) {
		int ped_id = Integer.parseInt(id);
		Pedido ped = pedidoRespository.findById(ped_id).orElse(null);
		
		if(ped.getEstado().equals("EN CAMINO")) {
			ped.setEstado("Recibido");
			
			return pedidoRespository.save(ped);
		}
		
		return null;
	}
	
	
	//Process a pending order 
	@PostMapping("PGPI/api/backend/pedido/pending_order")
    public List<Object> process_pending_order(@RequestParam List<Integer> prod_ids_restock, String id) {
		int ped_id = Integer.parseInt(id);
		for (Integer prod_id: prod_ids_restock) {
			addProductID(prod_id);
		}
		return get_ins_pos_order(pedidoRespository.findById(ped_id).orElse(null));
	}
    
    
    //Return Product positions for a specific order
    @GetMapping("PGPI/api/backend/pedido/pedidoid_pos")
    public List<Object> pedido_pos(@RequestParam String id){
    	int ped_id = Integer.parseInt(id);
    	Pedido pedido = pedidoRespository.findById(ped_id).orElse(null);
    	List<Object> posiciones_pedido = new ArrayList<Object>();
    	
    	if(pedido.getEstado().equals("PREPARACION")) {
    		List<Integer> id_vals = get_values_string(pedido.getId_producto());
    		List<Integer> cant_vals = get_values_string(pedido.getCantidad());
    	    	
    		for(int i = 0; i < id_vals.size(); i++) {
    	    	List<Object> pos_ins = get_ins_pos_product(pedidoRespository.findAll(), pedido, id_vals.get(i), cant_vals.get(i));
    	    	posiciones_pedido.add(pos_ins.get(0));
    	    }
    	    posiciones_pedido.add(pedido);
    	    return posiciones_pedido;
    	}
    	
    	return null;
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
    	    	pedido.setEstado("EN CAMINO");
    	    	pedidoRespository.save(pedido);
    	    	return true;
    		}

    	}
    	
    	return false;
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
 
    //Used when there is an order, to get positions and instances
	private List<Object> InstancesProducts(int id_producto, int cantidad, int cantidad_resv) {
    	List<Posicion> posiciones = posicionRespository.findAll();
    	List<Instancia_Producto> del_instancias = new ArrayList<Instancia_Producto>();
    	List<Posicion> del_posiciones = new ArrayList<Posicion>();
    	List<Object> pos_ins = new ArrayList<Object>();
	    	
    	while((cantidad_resv + cantidad > 0)) {
	        for (Instancia_Producto ins: instancias_disp) {
	       		if((ins.getIdproducto() == id_producto) & (ins.getDisponible() == 0)) {
	       			for (Posicion pos: posiciones) {
	       				if (pos.getId() == ins.getIdposicion()) {
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
    

    //Delete all tables
    @PostMapping("PGPI/api/backend/deleteAll")
    public void deleteAll(){
    	pedidoRespository.deleteAll();
    	productoRespository.deleteAll();
    	usuarioRespository.deleteAll();
    	posicionRespository.deleteAll();
    	instanciaProductoRespository.deleteAll();
    }
    

    //Returns the column for a set product
    public int return_position_product(int id_producto, List<Posicion> posiciones, List<Instancia_Producto> instancias) {
    	for (Posicion pos: posiciones) {
    		if(pos.getFila() == 2){
    			for (Instancia_Producto instan: instancias) {
    				if((id_producto == instan.getIdproducto()) & (pos.getId() == instan.getIdposicion())) {
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
    			pos.setTipo("PREPARACION");
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
	    			if(pos.getId() == instan.getIdposicion()) {
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
