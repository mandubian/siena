package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;
import siena.base.test.BaseTest;
import siena.core.lifecycle.LifeCyclePhase;
import siena.core.lifecycle.PostFetch;
import siena.core.lifecycle.PreFetch;

@Table("discoveries_lifecycle")
public class DiscoveryLifeCycleMulti {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer")
	public PersonLongAutoID discoverer;

	public DiscoveryLifeCycleMulti(String name, PersonLongAutoID discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public DiscoveryLifeCycleMulti() {
	}

	@PreFetch
	@PostFetch
	public void doit(LifeCyclePhase lcp) {
		System.out.println("doit for "+lcp);
		BaseTest.lifeCyclePhase += lcp.toString() + " ";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryLifeCycleMulti other = (DiscoveryLifeCycleMulti) obj;
		
		if(other.name != null && other.name.equals(name))
			return true;
		if(other.discoverer != null && other.discoverer.equals(discoverer))
			return true;
		
		return false;
	}
	
	public boolean isOnlyIdFilled() {
		if(this.id != null 
			&& this.name == null
			&& this.discoverer == null
		) return true;
		return false;
	}
	
	public String toString() {
		return "Discovery [ id:"+id+" - name:"+name+" - discoverer:"+discoverer+" ]";
	}
}
