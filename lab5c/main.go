package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type Arrays struct {
	arrayList [][]int
	mutex     sync.Mutex
	stopFlag  bool
	randGen   *rand.Rand
	barrier   sync.WaitGroup
}

func NewArrays(n, arraySize int) *Arrays {
	randSrc := rand.NewSource(time.Now().UnixNano())
	randGen := rand.New(randSrc)

	return &Arrays{
		arrayList: initializeArray(n, arraySize, randGen),
		stopFlag:  false,
		randGen:   randGen,
		barrier:   sync.WaitGroup{},
	}
}

func initializeArray(n, arraySize int, randGen *rand.Rand) [][]int {
	arrayList := make([][]int, n)
	for i := range arrayList {
		arrayList[i] = generateArray(arraySize, randGen)
	}
	return arrayList
}

func generateArray(arraySize int, randGen *rand.Rand) []int {
	array := make([]int, arraySize)
	for i := range array {
		array[i] = randGen.Intn(10)
	}
	return array
}

func printArrays(arr *Arrays) {
	for _, a := range arr.arrayList {
		fmt.Println(a)
	}
	fmt.Println()
}

func SimulateArrays(arr *Arrays, n, arraySize int) {
	for !arr.stopFlag {
		var wg sync.WaitGroup
		arr.barrier.Add(n)

		for i := 0; i < n; i++ {
			wg.Add(1)
			go ModifyElement(arr, i, arraySize, &wg)
		}
		wg.Wait()

		arr.barrier.Wait()

		if CheckArrayRule(arr, n) {
			arr.stopFlag = true
			fmt.Println("Однакова сума")
		}
		printArrays(arr)
	}
}

func ModifyElement(arr *Arrays, index, arraySize int, wg *sync.WaitGroup) {
	defer wg.Done()
	elemToChange := arr.randGen.Intn(arraySize)
	operation := arr.randGen.Intn(2)
	arr.mutex.Lock()
	defer arr.mutex.Unlock()
	if !arr.stopFlag {
		switch operation {
		case 0:
			if arr.arrayList[index][elemToChange] < 10 {
				arr.arrayList[index][elemToChange]++
			}
		case 1:
			if arr.arrayList[index][elemToChange] > -10 {
				arr.arrayList[index][elemToChange]--
			}
		}
	}

	arr.barrier.Done()
}

func CheckArrayRule(arr *Arrays, n int) bool {
	arr.mutex.Lock()
	defer arr.mutex.Unlock()
	if arr.stopFlag {
		fmt.Println(arr.arrayList)
		return true
	}
	sum := make([]int, n)
	for i, a := range arr.arrayList {
		for _, val := range a {
			sum[i] += val
		}
	}
	fmt.Println("Сума: ", sum)
	return CheckAllEqual(sum)
}

func CheckAllEqual(array []int) bool {
	for i := range array {
		if array[0] != array[i] {
			return false
		}
	}
	return true
}

func main() {
	const (
		N       = 3
		ArrSize = 5
	)

	arr := NewArrays(N, ArrSize)
	SimulateArrays(arr, N, ArrSize)
}
