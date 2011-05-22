package siena.base.test.model;

import siena.Column;
import siena.Generator;
import siena.Id;
import siena.Max;
import siena.Table;
import siena.base.test.BaseTest;
import siena.core.lifecycle.LifeCyclePhase;
import siena.core.lifecycle.PostDelete;
import siena.core.lifecycle.PostFetch;
import siena.core.lifecycle.PostInsert;
import siena.core.lifecycle.PostSave;
import siena.core.lifecycle.PostUpdate;
import siena.core.lifecycle.PreDelete;
import siena.core.lifecycle.PreFetch;
import siena.core.lifecycle.PreInsert;
import siena.core.lifecycle.PreSave;
import siena.core.lifecycle.PreUpdate;

@Table("discoveries_lifecycle")
public class DiscoveryLifeCycle {

	@Id(Generator.AUTO_INCREMENT)
	public Long id;
	
	@Max(100)
	public String name;
	
	@Column("discoverer")
	public PersonLongAutoID discoverer;

	public DiscoveryLifeCycle(String name, PersonLongAutoID discoverer) {
		this.name = name;
		this.discoverer = discoverer;
	}
	
	public DiscoveryLifeCycle() {
	}

	@PreFetch
	public void preFetch() {
		System.out.println("preFetch");
		BaseTest.lifeCyclePhase += LifeCyclePhase.PRE_FETCH.toString() + " ";
	}

	@PostFetch
	public void postFetch() {
		System.out.println("postFetch");
		BaseTest.lifeCyclePhase += LifeCyclePhase.POST_FETCH.toString() + " ";
	}
	
	@PreInsert
	public void preInsert() {
		System.out.println("preInsert");
		BaseTest.lifeCyclePhase += LifeCyclePhase.PRE_INSERT.toString() + " ";
	}

	@PostInsert
	public void postInsert() {
		System.out.println("postInsert");
		BaseTest.lifeCyclePhase += LifeCyclePhase.POST_INSERT.toString() + " ";
	}

	@PreDelete
	public void preDelete() {
		System.out.println("preDelete");
		BaseTest.lifeCyclePhase += LifeCyclePhase.PRE_DELETE.toString() + " ";
	}

	@PostDelete
	public void postDelete() {
		System.out.println("postDelete");
		BaseTest.lifeCyclePhase += LifeCyclePhase.POST_DELETE.toString() + " ";
	}

	@PreUpdate
	public void preUpdate() {
		System.out.println("preUpdate");
		BaseTest.lifeCyclePhase += LifeCyclePhase.PRE_UPDATE.toString() + " ";
	}

	@PostUpdate
	public void postUpdate() {
		System.out.println("postUpdate");
		BaseTest.lifeCyclePhase += LifeCyclePhase.POST_UPDATE.toString() + " ";
	}

	@PreSave
	public void preSave() {
		System.out.println("preSave");
		BaseTest.lifeCyclePhase += LifeCyclePhase.PRE_SAVE.toString() + " ";
	}

	@PostSave
	public void postSave() {
		System.out.println("postSave");
		BaseTest.lifeCyclePhase += LifeCyclePhase.POST_SAVE.toString() + " ";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;

		DiscoveryLifeCycle other = (DiscoveryLifeCycle) obj;
		
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
