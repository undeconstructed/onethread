package onethread;

/**
 * Actually a value, but matches the future contract.
 */
class ValueFuture<T> extends Future<T> {

	final T value;

	ValueFuture(T value) {
		this.value = value;
	}
}
