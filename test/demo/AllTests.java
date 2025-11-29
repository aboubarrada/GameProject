package demo;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({"demo.entities", "demo.managers"})
public class AllTests {
}
