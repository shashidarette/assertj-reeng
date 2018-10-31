for file in `find . -name '*.java'`
do
    # modified the grep pattern to find all the comment lines between /****...*/ especially for assertj-core system
    total=$(grep "\\*" $file | wc -l)
    echo "$file,$total"
done
