package my.pack;

import javax.ejb.Stateless;

import util.pack.MyModel;

public @Stateless class TigerBeanBean implements TigerBean {
	public void doSomething() {
		MyModel model = new MyModel();
		System.out.println(model);
	}
}
