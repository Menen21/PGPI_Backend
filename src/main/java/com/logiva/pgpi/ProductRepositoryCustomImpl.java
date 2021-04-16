package com.logiva.pgpi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom{

	@Autowired
	@Lazy
    ProductRepository productoRespository;
	@Autowired
    InstanciaProductoRepository instanciaProductoRespository;
	@Autowired
    PosicionRepository posicionRespository;
	
	@Override
	public List<Integer> availableProducts(Pedido pedido) {
		List<Integer> id_vals = get_values_string(pedido.getId_producto());
		List<Integer> cant_vals = get_values_string(pedido.getCantidad());
		List<Integer> prods_no_stock = new ArrayList<Integer>();
		List<Producto> productos = productoRespository.findByIdIn(id_vals);
		
		for(int i = 0; i < productos.size(); i++) {
			if(productos.get(i).getCantidad() < cant_vals.get(i)) {
				prods_no_stock.add(productos.get(i).getId());
			}
		}
		
		return prods_no_stock;
		
	}

	@Override
	public List<Producto> findProductsByIdString(String products) {
		List<Producto> productos = new ArrayList<Producto>();
		List<Integer> id_vals = get_values_string(products);
		for (Integer id: id_vals) {
			Producto p = productoRespository.findById(id).orElse(null);
			productos.add(p);
		}
		return productos;
		
	}
	
	//DOBLE
	public List<Integer> get_values_string(String values) {
		return Arrays.stream(values.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
	}

	@Override
	public List<Producto_cantidades> findProductoCantidades() {
		List<Producto> productos = productoRespository.findAll();
    	List<Producto_cantidades> productos_cantidades = new ArrayList<Producto_cantidades>();
    	Boolean restock;

    	for (Producto p: productos) {
    		int prep = countProductosPosicion(p.getId(), "Preparacion");
    		int stock = countProductosPosicion(p.getId(), "Stock");
    		if(p.getCantidad_minima_restock()>=p.getCantidad()) {
    			restock = true;
    		}
    		else {
    			restock = false;
    		}
    		productos_cantidades.add(new Producto_cantidades(p, stock, prep, restock));
    	}
        return productos_cantidades;
        
        
	}
	
	public int countProductosPosicion(int id, String tipo) {
		List<Instancia_Producto> instancias = instanciaProductoRespository.findByIdproducto(id);
		List<Integer> ids = new ArrayList<Integer>();
		for (Instancia_Producto ins: instancias) {
			ids.add(ins.getIdposicion());
		}
		List<Posicion> count = posicionRespository.findByIdInAndTipo(ids, tipo);
		return count.size();
	}

	@Override
	public List<Object> saveProducto(Producto producto, int cantidad) {
    	if (cantidad > 40) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Product maximum quantity is 40.");
		}
    	int amount = cantidad;
    	Producto p = productoRespository.findByNombre(producto.getNombre());
    	List<Object> returns = new ArrayList<Object>();
    	
		//If it exists we just add the amount to existing stock if there is space
    	if ((p != null)){
    		if ((p.getCantidad() != 0) & ((p.getCantidad() > 20) || (amount > 20))) {
    			throw new ResponseStatusException(HttpStatus.CONFLICT, "Full, no space available for this product.");
    		}
    		
    		p.setCantidad(p.getCantidad() + cantidad);
    		p = productoRespository.save(p);
    		returns.addAll(createInstancesProductos(amount, p.getId()));
    			
    		int stock = countProductosPosicion(p.getId(), "Stock");
        	int preparacion = countProductosPosicion(p.getId(), "Preparacion");
        	
        	returns.add(new Producto_cantidades(p, stock, preparacion));
    		return returns;
    	}
    	//If the product is new, we add it.
    	producto = productoRespository.save(producto);
    	returns.addAll(createInstancesProductos(amount, producto.getId()));
    	int stock = countProductosPosicion(producto.getId(), "Stock");
    	int preparacion = countProductosPosicion(producto.getId(), "Preparacion");
    	returns.add(new Producto_cantidades(producto, stock, preparacion));
        return returns;

	}
	
	
	//Used to create product instances, used when adding products
    public List<Instancia_Producto> createInstancesProductos(int cantidad, int id_producto) {
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
    	instanciaProductoRespository.saveAll(new_instancias);
    	return new_instancias;
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
    
    //DOBLE
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

}
