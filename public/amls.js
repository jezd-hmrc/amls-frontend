/*global $*/
// TODO: Tidy up
$(function () {
  // avoids double panel-indented sections
  // should be CSS but isn't possible.
  // TODO: This needs a different approach
  $('.panel-indent > .form-field--error')
    .filter(function () {
      return $(this).siblings().not('legend').length === 0;
    })
    .parent().css({
      'padding': 0,
      'border': 'none'
    });

  $.widget('hmrc.auto', {
    _create: function () {

      var options, _select, _change, input, value;

      value = this.element.find('option:selected').text();

      this.element.hide();

      this.element.parents('form').submit(function (e) {
        _change();
      });

      options = this.element
        .find('option')
        .toArray()
        .map(function (elem) {
          return {
            label: elem.text,
            value: elem.text,
            option: elem
          };
        });

      _select = function (event, ui) {
        ui.item.option.selected = true;
      };

      _change = function (event, ui) {
        options.forEach(function (e) {
          e.option.selected = e.label.toLowerCase() === input.val().toLowerCase();
        });
      };

      input = $('<input>')
        .insertAfter(this.element)
        .val(value)
        .autocomplete({
          source: options,
          select: _select,
          change: _change
        });
    }
  });

  $('select[data-auto-complete]').auto();

  $('*[data-add-btn]').click(function () {
    $('select[data-auto-complete]').combobox();
  });

  (function () {
    $.widget('custom.addOne', {
      _create: function () {
        var $this = $(this.element);
        var text = $this.data('add-one');
        var children = $this.children();

        children
          .filter(':not(:first):not(:has(option[selected]))')
          .addClass('js-hidden');

        var $button = $('<a href="#">' + text + '</a>').click(function (e) {
          e.preventDefault();
          $this.find('div.js-hidden:first').fadeIn(500).removeClass('js-hidden');
          if ($this.find('div.js-hidden').size() === 0) {
            $(this).hide();
          }
        });

        $this.append($button.hide().fadeIn(1000));
      }
    });
    $('*[data-add-one]').addOne({});
  })();

  (function () {
    var checkedInputs = 'input[type="checkbox"], input[type="radio"]';

    $('input[data-toggle]').each(function () {
      var $self = $(this),
        $target = $($self.data('toggle')),
        $inputs = $target.find('input, option, selected, textarea');

      function pred() {
        var $this = $(this),
          hasValue = false;

        if ($this.is(checkedInputs)) {
          if ($this.prop('checked')) {
            hasValue = true;
          }
        } else if ($this.is('input') && $this.val() !== '') {
          hasValue = true;
        } else if ($this.is('option') && ($this.prop('selected') && $this.val() !== '')) {
          hasValue = true;
        }
        return hasValue;
      }

      if ($target.attr('data-toggle-new')) {
        if ($inputs.filter(pred).length || $self.prop('checked') === true) {
          $target.show();
        }
      } else {
        if ($inputs.filter(pred).length === 0) {
          if ($self.prop('checked') === false) {
            $target.hide();
          }
        }
      }

      function hide() {
        $inputs.filter(checkedInputs).prop('checked', false);
        $inputs.filter('input, select, textarea').val('');
        $inputs.filter('option').prop('selected', false);
        $target.hide();
      }

      $self.change(function () {
        if ($self.prop('checked') === true) {
          $target.show();
        } else {
          hide();
        }
      });

      if ($self.prop('type') !== "checkbox") {
        $('input[name="' + $self.prop('name') + '"][value!="' + $self.val() + '"]').change(function () {
          hide();
        });
      }

      if ($self.prop('checked') === true) {
        $target.show();
      }
    });
  }());
});
